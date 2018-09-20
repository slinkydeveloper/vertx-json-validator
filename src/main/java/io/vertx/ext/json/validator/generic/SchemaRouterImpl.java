package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchemaRouterImpl implements SchemaRouter {

  final Map<URI, RouterNode> absolutePaths;
  final HttpClient client;
  final FileSystem fs;
  final Map<URI, List<Future<Schema>>> externalSchemasSolving;

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
        RouterNode nodeOfScope = absolutePaths.get(scope.getURIWithoutFragment());
        node = absolutePaths
            .entrySet()
            .stream()
            .filter(e -> e.getValue().equals(nodeOfScope))
            .map(e -> URIUtils.replaceOrResolvePath(e.getKey(), refURI.getPath()))
            .map(absolutePaths::get)
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

  private synchronized boolean isSchemaAlreadySolving(URI u) {
    return externalSchemasSolving.containsKey(u);
  }

  private synchronized void subscribeSchemaSolvedFuture(URI u, Future<Schema> fut) {
    if (externalSchemasSolving.containsKey(u))
      externalSchemasSolving.get(u).add(fut);
    else {
      List<Future<Schema>> futs = new ArrayList<>();
      futs.add(fut);
      externalSchemasSolving.put(u, futs);
    }
  }

  private synchronized void triggerUnsolvedSchema(URI u, final Throwable e) {
    List<Future<Schema>> futs = externalSchemasSolving.remove(u);
    futs.forEach(f -> f.fail(e));
  }

  private synchronized void triggerSolvedSchema(URI u, final Schema s) {
    List<Future<Schema>> futs = externalSchemasSolving.remove(u);
    futs.forEach(f -> f.complete(s));
  }

  private void triggerExternalRefSolving(final JsonPointer pointer, final JsonPointer scope, final SchemaParser schemaParser) {
    final URI u = pointer.getURIWithoutFragment();
    if ("http".equals(u.getScheme()) || "https".equals(u.getScheme())) {
      client.getAbs(u.toString(),res -> {
        res.exceptionHandler(e -> triggerUnsolvedSchema(u, e));
        if (res.statusCode() == 200) {
          res.bodyHandler(buf -> {
            try {
              schemaParser.parseSchemaFromString(buf.toString(), u);
              triggerSolvedSchema(u, this.resolveCachedSchema(pointer, scope));
            } catch (SchemaException e) {
              triggerUnsolvedSchema(u, e);
            }
          });
        } else {
          triggerUnsolvedSchema(u, ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO wrong status code
        }
      }).putHeader(HttpHeaders.ACCEPT.toString(), "application/json, application/schema+json").end();
    } else {
      URI fileURI = scope.getURIWithoutFragment().resolve(u);
      fs.readFile(fileURI.toString(), res -> {
        if (res.succeeded()) {
          try {
            schemaParser.parseSchemaFromString(res.result().toString(), u);
            triggerSolvedSchema(u, this.resolveCachedSchema(pointer, scope));
          } catch (SchemaException e) {
            triggerUnsolvedSchema(u, e);
          }
        } else {
          triggerUnsolvedSchema(u, ValidationExceptionFactory.generateNotMatchValidationException("")); //TODO use fail
        }
      });
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
      Future<Schema> fut = Future.future();
      subscribeSchemaSolvedFuture(u, fut);
      return fut;
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
