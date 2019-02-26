package io.vertx.ext.json.validator.generic;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaException;
import io.vertx.ext.json.validator.SchemaParser;
import io.vertx.ext.json.validator.SchemaRouter;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaRouterImpl implements SchemaRouter {

  final Map<URI, RouterNode> absolutePaths;
  final HttpClient client;
  final FileSystem fs;
  final Map<URI, ObservableFuture<Schema>> externalSchemasSolving;

  public SchemaRouterImpl(HttpClient client, FileSystem fs) {
    this.client = client;
    this.fs = fs;
    absolutePaths = new HashMap<>();
    this.externalSchemasSolving = new ConcurrentHashMap<>();
  }

  @Override
  public List<Schema> registeredSchemas() {
    return absolutePaths
        .values()
        .stream()
        .flatMap(RouterNode::flattened)
        .map(RouterNode::getSchema)
        .collect(Collectors.toList());
  }

  @Override
  public Schema resolveCachedSchema(JsonPointer refPointer, JsonPointer scope, final SchemaParser parser) {
    return resolveParentNode(refPointer, scope).flatMap(parentNode -> {
      Optional<RouterNode> resultNode = Optional.ofNullable((RouterNode) refPointer.query(new RouterNodeJsonPointerIterator(parentNode)));
      if (resultNode.isPresent())
        return resultNode.map(RouterNode::getSchema);
      if (parentNode.getSchema() instanceof SchemaImpl) // Maybe the schema that we are searching was not parsed yet!
        return Optional.ofNullable(refPointer.queryJson(parentNode.getSchema().getJson()))
          .map(queryResult -> parser.parse(queryResult, URIUtils.replaceFragment(parentNode.getSchema().getScope().getURIWithoutFragment(), refPointer.build())));
      return Optional.empty();
    }).orElse(null);
  }

  @Override
  public Future<Schema> resolveRef(final JsonPointer pointer, final JsonPointer scope, final SchemaParser schemaParser) {
    try {
      Schema cachedSchema = this.resolveCachedSchema(pointer, scope, schemaParser);
      if (cachedSchema == null) {
        return resolveExternalRef(pointer, scope, schemaParser);
      } else return Future.succeededFuture(cachedSchema);
    } catch (SchemaException e) {
      return Future.failedFuture(e);
    }
  }

  @Override
  public void addSchema(Schema schema) {
    URI schemaScopeWithoutFragment = schema.getScope().getURIWithoutFragment();
    absolutePaths.putIfAbsent(schemaScopeWithoutFragment, new RouterNode());

    RouterNode parentNode = absolutePaths.get(schemaScopeWithoutFragment);
    RouterNodeJsonPointerIterator iterator = new RouterNodeJsonPointerIterator(parentNode);
    schema.getScope().write(
        iterator,
        schema,
        true
    );

    // Handle $id keyword
    if (schema instanceof SchemaImpl && ((SchemaImpl) schema).getJson().containsKey("$id")) {
      try {
        String unparsedId = ((SchemaImpl) schema).getJson().getString("$id");
        URI id = URI.create(unparsedId);
        JsonPointer idPointer = URIUtils.createJsonPointerFromURI(id);
        // Create parent node aliases if needed
        if (id.isAbsolute()) { // Absolute id
          absolutePaths.putIfAbsent(URIUtils.removeFragment(id), iterator.getCurrentValue()); // id and inferredScope can match!
        } else if (id.getPath() != null && !id.getPath().isEmpty()) {
          // If a path is relative you should solve the path/paths. The paths will be solved against aliases of base node of inferred scope
          List<URI> paths = absolutePaths
              .entrySet()
              .stream()
              .filter(e -> e.getValue().equals(parentNode))
              .map(e -> URIUtils.resolvePath(e.getKey(), id.getPath()))
              .collect(Collectors.toList());
          paths.forEach(u -> absolutePaths.put(u, iterator.getCurrentValue()));
        }
        // Write the alias down the tree
        if (!idPointer.isRootPointer())
          idPointer.write(new RouterNodeJsonPointerIterator(parentNode), iterator.getCurrentValue(), true);
      } catch (IllegalArgumentException e) {
        throw new SchemaException(schema, "$id keyword should be a valid URI", e);
      }
    }
  }

  // The idea is to traverse from base to actual scope all tree and find aliases
  private Stream<URI> getScopeParentAliases(JsonPointer scope) {
    Stream.Builder<URI> uriStreamBuilder = Stream.builder();
    RouterNode startingNode = absolutePaths.get(scope.getURIWithoutFragment());
    scope.query(new RouterNodeJsonPointerIterator(startingNode, node ->
        absolutePaths.forEach((uri, n) -> { if (n == node) uriStreamBuilder.accept(uri); })
    ));
    return uriStreamBuilder.build();
  }

  private Optional<RouterNode> resolveParentNode(JsonPointer refPointer, JsonPointer scope) {
    URI refURI = refPointer.getURIWithoutFragment();
    if (!refURI.isAbsolute()) {
      if (refURI.getPath() != null && !refURI.getPath().isEmpty()) {
        // Path pointer
        return getScopeParentAliases(scope)
            .map(e -> URIUtils.resolvePath(e, refURI.getPath()))
            .map(absolutePaths::get)
            .filter(Objects::nonNull)
            .findFirst();
      } else {
        // Fragment pointer, fallback to scope
        return Optional.ofNullable(absolutePaths.get(scope.getURIWithoutFragment()));
      }
    } else {
      // Absolute pointer
      return Optional.ofNullable(absolutePaths.get(refURI));
    }
  }

  private Future<String> solveRemoteRef(final URI ref) {
    Future<String> fut = Future.future();
    client.getAbs(ref.toString(), res -> {
      res.exceptionHandler(fut::fail);
      if (res.statusCode() == 200) {
        res.bodyHandler(buf -> {
          try {
            fut.complete(buf.toString());
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

  private Future<String> solveLocalRef(final URI ref) {
    Future<String> fut = Future.future();
    String filePath = ("jar".equals(ref.getScheme())) ? ref.getSchemeSpecificPart().split("!")[1].substring(1) : ref.getPath();
    fs.readFile(filePath, res -> {
      if (res.succeeded()) {
        try {
          fut.complete(res.result().toString());
        } catch (SchemaException e) {
          fut.fail(e);
        }
      } else {
        fut.fail(res.cause());
      }
    });
    return fut;
  }

  private ObservableFuture<Schema> resolveExternalRef(final JsonPointer pointer, final JsonPointer scope, final SchemaParser schemaParser) {
    URI refURI = pointer.getURIWithoutFragment();
    return externalSchemasSolving.computeIfAbsent(refURI, (r) -> {
      Stream<URI> candidatesURIs;
      if (refURI.isAbsolute()) // $ref uri is absolute, just solve it
        candidatesURIs = Stream.of(refURI);
      else // $ref is relative, so it should resolve all aliases of scope and then relativize
        candidatesURIs = getScopeParentAliases(scope)
            .map(u -> URIUtils.resolvePath(u, refURI.getPath()))
            .filter(u -> URIUtils.isRemoteURI(u) || URIUtils.isLocalURI(u)) // Remove aliases not resolvable
            .sorted((u1, u2) -> (URIUtils.isLocalURI(u1) && !URIUtils.isLocalURI(u2)) ? 1 : (u1.equals(u2)) ? 0 : -1); // Try to solve local refs before
      return ObservableFuture.wrap(
          CompositeFuture.any(
            candidatesURIs
              .map(u ->
                  (URIUtils.isRemoteURI(u)) ?
                      solveRemoteRef(u).map(s -> schemaParser.parseSchemaFromString(s, JsonPointer.fromURI(u))) :
                      solveLocalRef(u).map(s -> schemaParser.parseSchemaFromString(s, JsonPointer.fromURI(u))))
                .collect(Collectors.toList())
          ).map(cf ->
            cf.list().stream()
                .filter(Objects::nonNull)
                .map(o -> (Schema)o)
                .findFirst().orElse(null)
          )
      );
    });
  }

  @Override
  public Future<Schema> solveAllSchemaReferences(Schema schema) {
    if (schema instanceof RefSchema) {
      return ((RefSchema) schema)
          .trySolveSchema()
          .compose(s -> (s != schema) ? solveAllSchemaReferences(s).map(schema) : Future.succeededFuture(schema));
    } else {
      RouterNode node = absolutePaths.get(schema.getScope().getURIWithoutFragment());
      node = (RouterNode) schema.getScope().query(new RouterNodeJsonPointerIterator(node));
      return CompositeFuture.all(
          node
              .reverseFlattened()
              .collect(Collectors.toList())// Must create a collection to avoid ConcurrentModificationException
              .stream()
              .map(RouterNode::getSchema)
              .filter(Objects::nonNull)
              .filter(s -> s instanceof RefSchema)
              .map(s -> (RefSchema)s)
              .map(RefSchema::trySolveSchema)
              .collect(Collectors.toList())
      ).map(schema);
    }
  }

}
