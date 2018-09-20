package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaRouterImpl implements SchemaRouter {

  final Map<URI, RouterNode> absolutePaths;
  final HttpClient client;
  final FileSystem fs;
  final Map<URI, List<Future<URI>>> externalSchemasSolving;

  public SchemaRouterImpl(HttpClient client, FileSystem fs) {
    absolutePaths = new HashMap<>();
    this.client = client;
    this.fs = fs;
    this.externalSchemasSolving = new HashMap<>();
  }

  @Override
  public Schema resolveCachedSchema(JsonPointer refPointer, JsonPointer scope) {
    URI refURI = refPointer.getURIWithoutFragment();
    RouterNode node;
    if (!refURI.isAbsolute()) {
      // Fragment pointer or path pointer!
      if (refURI.getPath() != null && !refURI.getPath().isEmpty()) {
        node = getParentsURIs(scope)
            .map(e -> URIUtils.replaceOrResolvePath(e, refURI.getPath()))
            .map(absolutePaths::get)
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
      } else { // Fallback to scope
        node = absolutePaths.get(scope.getURIWithoutFragment());
      }
    } else {
      node = absolutePaths.get(refURI);
    }
    if (node == null) return null;
    node = (RouterNode) refPointer.query(new RouterNodeJsonPointerIterator(node));
    if (node == null) return null;
    else return node.getThisSchema();
  }

  // The idea is to traverse from base to actual scope all tree and find aliases
  private Stream<URI> getParentsURIs(JsonPointer scope) {
    Stream.Builder<URI> uriStreamBuilder = Stream.builder();
    RouterNode startingNode = absolutePaths.get(scope.getURIWithoutFragment());
    scope.query(new RouterNodeJsonPointerIterator(startingNode, node ->
      absolutePaths.forEach((uri, n) -> { if (n == node) uriStreamBuilder.accept(uri); })
    ));
    return uriStreamBuilder.build();
  }

  private synchronized boolean isSchemaAlreadySolving(URI u) {
    return externalSchemasSolving.containsKey(u);
  }

  private synchronized void subscribeSchemaSolvedFuture(URI u, Future fut) {
    if (externalSchemasSolving.containsKey(u))
      externalSchemasSolving.get(u).add(fut);
    else {
      List<Future<URI>> futs = new ArrayList<>();
      futs.add(fut);
      externalSchemasSolving.put(u, futs);
    }
  }

  private synchronized void triggerUnsolvedSchema(URI u, final Throwable e) {
    List<Future<URI>> futs = externalSchemasSolving.remove(u);
    futs.forEach(f -> f.fail(e));
  }

  private synchronized void triggerSolvedSchema(URI registeredURI, URI findedURI) {
    List<Future<URI>> futs = externalSchemasSolving.remove(registeredURI);
    futs.forEach(f -> f.complete(findedURI));
  }

  private Future<URI> solveRemoteRef(final URI ref, final SchemaParser schemaParser) {
    Future<URI> fut = Future.future();
    client.getAbs(ref.toString(),res -> {
      res.exceptionHandler(fut::fail);
      if (res.statusCode() == 200) {
        res.bodyHandler(buf -> {
          try {
            schemaParser.parseSchemaFromString(buf.toString(), ref);
            fut.complete(ref);
          } catch (SchemaException e) {
            fut.fail(e);
          }
        });
      } else {
        fut.fail(ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO wrong status code
      }
    }).putHeader(HttpHeaders.ACCEPT.toString(), "application/json, application/schema+json").end();
    return fut;
  }

  private Future<URI> solveLocalRef(final URI ref, final URI scope, final SchemaParser schemaParser) {
    Future<URI> fut = Future.future();
    URI fileURI = scope.resolve(ref);
    fs.readFile(fileURI.toString(), res -> {
      if (res.succeeded()) {
        try {
          schemaParser.parseSchemaFromString(res.result().toString(), ref);
          fut.complete(ref);
        } catch (SchemaException e) {
          fut.fail(e);
        }
      } else {
        fut.fail(ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO use fail
      }
    });
    return fut;
  }

  private void triggerExternalRefSolving(final JsonPointer pointer, final JsonPointer scope, final SchemaParser schemaParser) {
    URI ref = pointer.getURIWithoutFragment();
    if (!ref.isAbsolute()) { // Only a path should be, otherwise the $ref is wrong!
      CompositeFuture.any(
          getParentsURIs(scope)
              .map(u -> URIUtils.replaceOrResolvePath(u, ref.getPath()))
              .map(u -> {
                if (URIUtils.isRemoteURI(u))
                  return solveRemoteRef(u, schemaParser);
                else if (!URIUtils.isRemoteURI(scope.getURIWithoutFragment()))
                  return solveLocalRef(u, scope.getURIWithoutFragment(), schemaParser);
                else return null;
              })
              .filter(Objects::nonNull)
              .collect(Collectors.toList())
      ).setHandler(ar -> {
        if (ar.succeeded()) triggerSolvedSchema(
            ref,
            ar.result().list().stream().filter(Objects::nonNull).map(o -> (URI)o).findFirst().orElse(null)
        );
        else triggerUnsolvedSchema(ref, ar.cause());
      });
    } else {
      if (URIUtils.isRemoteURI(ref)) {
        solveRemoteRef(ref, schemaParser).setHandler(ar -> {
          if (ar.succeeded()) triggerSolvedSchema(ref, ref);
          else triggerUnsolvedSchema(ref, ar.cause());
        });
      } else {
        solveLocalRef(ref, scope.getURIWithoutFragment(), schemaParser).setHandler(ar -> {
          if (ar.succeeded()) triggerSolvedSchema(ref, ref);
          else triggerUnsolvedSchema(ref, ar.cause());
        });
      }
    }
  }

  @Override
  public Future<Schema> resolveRef(final JsonPointer pointer, final JsonPointer scope, final SchemaParser schemaParser) {
    Schema cachedSchema = this.resolveCachedSchema(pointer, scope);
    if (cachedSchema == null) {
      URI u = pointer.getURIWithoutFragment();
      if (!isSchemaAlreadySolving(u)) {
        triggerExternalRefSolving(pointer, scope, schemaParser);
      }
      Future<URI> fut = Future.future();
      subscribeSchemaSolvedFuture(u, fut);
      return fut.compose(uri -> Future.succeededFuture(this.resolveCachedSchema(pointer, JsonPointer.fromURI(uri))));
    } else return Future.succeededFuture(cachedSchema);
  }

  @Override
  public void addSchema(Schema schema, JsonPointer inferredScope) {
    URI inferredScopeWithoutFragment = inferredScope.getURIWithoutFragment();
    if (absolutePaths.containsKey(inferredScopeWithoutFragment))
      inferredScope.write(
          new RouterNodeJsonPointerIterator(absolutePaths.get(inferredScopeWithoutFragment)),
          schema,
          true
      );
    else {
      RouterNode node = new RouterNode();
      absolutePaths.put(inferredScopeWithoutFragment, node);
      if (inferredScope.isRootPointer()) node.setThisSchema(schema);
      else inferredScope.write(
          new RouterNodeJsonPointerIterator(node),
          schema,
          true
      );
    }
    if (schema instanceof SchemaImpl) {
      if (((SchemaImpl) schema).getSchema().containsKey("$id")) {
        try {
          String unparsedId = ((SchemaImpl) schema).getSchema().getString("$id");
          URI id = URI.create(unparsedId);
          RouterNode baseNodeOfInferredScope = absolutePaths.get(inferredScopeWithoutFragment);
          RouterNode insertedSchemaNode = (RouterNode) inferredScope.query(new RouterNodeJsonPointerIterator(baseNodeOfInferredScope));
          if (id.isAbsolute()) {
            absolutePaths.putIfAbsent(URIUtils.removeFragment(id), insertedSchemaNode); // id and inferredScope can match!
          } else if (id.getPath() != null && !id.getPath().isEmpty()) {
            // If a path is relative you should solve the path/paths. The paths will be solved against aliases of base node of inferred scope
            List<URI> uris = absolutePaths
                .entrySet()
                .stream()
                .filter(e -> e.getValue().equals(baseNodeOfInferredScope))
                .map(e -> URIUtils.replaceOrResolvePath(e.getKey(), id.getPath()))
                .collect(Collectors.toList());
            uris.forEach(u -> absolutePaths.put(u, insertedSchemaNode));
          }
          JsonPointer idPointer = JsonPointer.fromURI(id);
          if (!idPointer.isRootPointer())
            idPointer.write(new RouterNodeJsonPointerIterator(baseNodeOfInferredScope), insertedSchemaNode, true);
        } catch (IllegalArgumentException e) {
          throw SchemaErrorType.WRONG_KEYWORD_VALUE.createException(schema, "$id keyword should be a valid URI");
        }
      }
    }
  }
}
