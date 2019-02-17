package io.vertx.ext.json.validator.generic;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(VertxUnitRunner.class)
public class ObservableFutureTest {

  @Test
  public void testAlreadyCompletedFuture(TestContext context) {
    Future<Void> completedFuture = Future.succeededFuture();

    ObservableFuture<Void> multiFuture = ObservableFuture.wrap(completedFuture);

    Async async = context.async(2);

    multiFuture.setHandler(context.asyncAssertSuccess(v -> {
      context.assertNull(v);
      async.countDown();
    }));

    multiFuture.setHandler(context.asyncAssertSuccess(v -> {
      context.assertNull(v);
      async.countDown();
    }));

    async.await(1000);
  }

  @Test
  public void testAlreadyFailedFuture(TestContext context) {
    Future<Void> failedFuture = Future.failedFuture(new IllegalStateException());

    ObservableFuture<Void> multiFuture = ObservableFuture.wrap(failedFuture);

    Async async = context.async(2);

    multiFuture.setHandler(context.asyncAssertFailure(v -> {
      assertThat(v).isInstanceOf(IllegalStateException.class);
      async.countDown();
    }));

    multiFuture.setHandler(context.asyncAssertFailure(v -> {
      assertThat(v).isInstanceOf(IllegalStateException.class);
      async.countDown();
    }));

    async.await(1000);
  }

  @Test
  public void testCompletedFuture(TestContext context) {
    Vertx vertx = Vertx.vertx();
    Future<String> fut = Future.future();
    ObservableFuture<String> multiFuture = ObservableFuture.wrap(fut);

    Async async = context.async(2);

    multiFuture.setHandler(context.asyncAssertSuccess(v -> {
      assertThat(v).isEqualTo("Hello");
      async.countDown();
    }));

    multiFuture.setHandler(context.asyncAssertSuccess(v -> {
      assertThat(v).isEqualTo("Hello");
      async.countDown();
    }));

    vertx.setTimer(1, l -> fut.complete("Hello"));
    async.await();
  }

  @Test
  public void testFailedFuture(TestContext context) {
    Vertx vertx = Vertx.vertx();
    Future<String> fut = Future.future();
    ObservableFuture<String> multiFuture = ObservableFuture.wrap(fut);

    Async async = context.async(2);

    multiFuture.setHandler(context.asyncAssertFailure(v -> {
      assertThat(v).isInstanceOf(IllegalStateException.class).hasMessage("Hello");
      async.countDown();
    }));

    multiFuture.setHandler(context.asyncAssertFailure(v -> {
      assertThat(v).isInstanceOf(IllegalStateException.class).hasMessage("Hello");
      async.countDown();
    }));

    vertx.setTimer(1, l -> fut.fail(new IllegalStateException("Hello")));
    async.await(1000);
  }

}
