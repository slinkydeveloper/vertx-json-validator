package io.vertx.ext.json.validator.generic;

import io.vertx.ext.json.pointer.JsonPointer;
import io.vertx.ext.json.validator.Schema;
import io.vertx.ext.json.validator.SchemaErrorType;
import io.vertx.ext.json.validator.SchemaRouter;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchemaRouterImpl implements SchemaRouter {

  final Map<URI, RouterNode> absolutePaths;

  public SchemaRouterImpl() {
    absolutePaths = new HashMap<>();
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
            absolutePaths.put(URIUtils.removeFragment(id), insertedSchemaNode);
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
