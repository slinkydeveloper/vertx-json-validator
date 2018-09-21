package io.vertx.ext.json.validator.generic;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class FutureUtils {

  public static <T> Future<T> oneOf(List<Future<T>> results) {
    final Future<T> res = Future.future();
    final AtomicInteger processed = new AtomicInteger(0);
    final AtomicBoolean atLeastOneOk = new AtomicBoolean(false);
    final AtomicReference<T> result = new AtomicReference<>();
    final int len = results.size();
    for (int i = 0; i < len; i++) {
      results.get(i).setHandler(ar -> {
        int p = processed.incrementAndGet();
        if (((AsyncResult) ar).succeeded()) {
          if (atLeastOneOk.get())
            res.tryFail(new IllegalStateException("One future was already completed"));
          else {
            atLeastOneOk.set(true);
            result.set(res.result());
          }
        }
        if (p == len) {
          if (atLeastOneOk.get()) res.tryComplete(result.get());
          else res.tryFail(new IllegalStateException("No future was completed"));
        }
      });
    }
    return res;
  }

  public static <T, U> Future<U> andThen(Future<T> fut, Function<T, Future<U>> completionMapper, Function<Throwable, Future<U>> failureMapper) {
    if (completionMapper == null || failureMapper == null) {
      throw new NullPointerException();
    }
    Future<U> ret = Future.future();
    fut.setHandler(ar -> {
      if (ar.succeeded()) {
        Future<U> apply;
        try {
          apply = completionMapper.apply(ar.result());
        } catch (Throwable e) {
          ret.fail(e);
          return;
        }
        apply.setHandler(ret);
      } else {
        Future<U> apply;
        try {
          apply = failureMapper.apply(ar.cause());
        } catch (Throwable e) {
          ret.fail(e);
          return;
        }
        apply.setHandler(ret);
      }
    });
    return ret;
  }

}
