package io.vertx.ext.json.schema.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.schema.*;

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
  public Schema resolveCachedSchema(JsonPointer refPointer, JsonPointer scope, final SchemaParser parser) {
    URI refURI = refPointer.getURIWithoutFragment();
    RouterNode node;
    if (!refURI.isAbsolute()) {
      // Fragment pointer or path pointer!
      if (refURI.getPath() != null && !refURI.getPath().isEmpty()) {
        node = getParentsURIs(scope)
            .map(e -> URIUtils.resolvePath(e, refURI.getPath()))
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
    RouterNode resultNode = (RouterNode) refPointer.query(new RouterNodeJsonPointerIterator(node));
    if (resultNode == null && node.getThisSchema() instanceof SchemaImpl) {
      // Maybe the schema that we are searching was not parsed!
      JsonObject baseSchemaToQuery = ((SchemaImpl)node.getThisSchema()).getSchema();
      Object queryResult = refPointer.queryJson(baseSchemaToQuery);
      if (queryResult == null) return null;
      return parser.parse(queryResult, URIUtils.replaceFragment(node.getThisSchema().getScope().getURIWithoutFragment(), refPointer.build()));
    }
    if (resultNode == null) return null;
    else return resultNode.getThisSchema();
  }

  @Override
  public Future<Schema> resolveRef(final JsonPointer pointer, final JsonPointer scope, final SchemaParser schemaParser) {
    try {
      Schema cachedSchema = this.resolveCachedSchema(pointer, scope, schemaParser);
      if (cachedSchema == null) {
        URI u = pointer.getURIWithoutFragment();
        if (!isSchemaAlreadySolving(u)) {
          triggerExternalRefSolving(pointer, scope, schemaParser);
        }
        Future<URI> fut = Future.future();
        subscribeSchemaSolvedFuture(u, fut);
        return fut.compose(uri -> Future.succeededFuture(this.resolveCachedSchema(pointer, JsonPointer.fromURI(uri), schemaParser)));
      } else return Future.succeededFuture(cachedSchema);
    } catch (SchemaException e) {
      return Future.failedFuture(e);
    }
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
                .map(e -> URIUtils.resolvePath(e.getKey(), id.getPath()))
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
            schemaParser.parseSchemaFromString(buf.toString(), JsonPointer.fromURI(ref));
            fut.complete(ref);
          } catch (SchemaException e) {
            fut.fail(e);
          }
        });
      } else {
        fut.fail(new IllegalStateException("Wrong status code " + res.statusCode() + "received while resolving remote ref"));
      }
    }).putHeader(HttpHeaders.ACCEPT.toString(), "application/json, application/schema+json").end();
    return fut;
  }

  private Future<URI> solveLocalRef(final URI ref, final SchemaParser schemaParser) {
    Future<URI> fut = Future.future();
    String filePath = ("jar".equals(ref.getScheme())) ? ref.getSchemeSpecificPart().split("!")[1].substring(1) : ref.getPath();
    fs.readFile(filePath, res -> {
      if (res.succeeded()) {
        try {
          schemaParser.parseSchemaFromString(res.result().toString(), JsonPointer.fromURI(ref));
          fut.complete(ref);
        } catch (SchemaException e) {
          fut.fail(e);
        }
      } else {
        fut.fail(res.cause());
      }
    });
    return fut;
  }

  private void triggerExternalRefSolving(final JsonPointer pointer, final JsonPointer scope, final SchemaParser schemaParser) {
    URI ref = pointer.getURIWithoutFragment();
    if (!ref.isAbsolute()) { // Only a path should be, otherwise the $ref is wrong!
      CompositeFuture.any(
          getParentsURIs(scope)
              .map(u -> URIUtils.resolvePath(u, ref.getPath()))
              .map(u -> {
                if (URIUtils.isRemoteURI(u))
                  return solveRemoteRef(u, schemaParser);
                else if (!URIUtils.isRemoteURI(scope.getURIWithoutFragment()))
                  return solveLocalRef(u, schemaParser);
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
        solveLocalRef(ref, schemaParser).setHandler(ar -> {
          if (ar.succeeded()) triggerSolvedSchema(ref, ref);
          else triggerUnsolvedSchema(ref, ar.cause());
        });
      }
    }
  }
}
