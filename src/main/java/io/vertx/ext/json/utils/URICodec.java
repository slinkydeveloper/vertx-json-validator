package io.vertx.ext.json.utils;

import io.vertx.core.json.JsonCodec;

import java.net.URI;

public class URICodec implements JsonCodec<URI, String> {

  public static class URICodecHolder {
    static final URICodec INSTANCE = new URICodec();
  }

  public static URICodec getInstance() { return URICodecHolder.INSTANCE; }

  @Override
  public URI decode(String value) {
    return URI.create(value);
  }

  @Override
  public String encode(URI value) {
    return value.toString();
  }
}
