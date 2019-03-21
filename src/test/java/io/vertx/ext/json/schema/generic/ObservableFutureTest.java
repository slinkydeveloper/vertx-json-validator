package io.vertx.ext.json.schema.generic;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class ObservableFutureTest {

  @Test
  public void testAlreadyCompletedFuture(VertxTestContext context) {
    Future<Void> completedFuture = Future.succeededFuture();

    ObservableFuture<Void> multiFuture = ObservableFuture.wrap(completedFuture);

    Checkpoint checkpoint = context.checkpoint(2);

    multiFuture.setHandler(context.succeeding(v -> {
      context.verify(() -> assertThat(v).isNull());
      checkpoint.flag();
    }));
    multiFuture.setHandler(context.succeeding(v -> {
      context.verify(() -> assertThat(v).isNull());
      checkpoint.flag();
    }));
  }

  @Test
  public void testAlreadyFailedFuture(VertxTestContext context) {
    Future<Void> failedFuture = Future.failedFuture(new IllegalStateException());

    ObservableFuture<Void> multiFuture = ObservableFuture.wrap(failedFuture);

    Checkpoint checkpoint = context.checkpoint(2);

    multiFuture.setHandler(context.failing(v -> {
      context.verify(() -> assertThat(v).isInstanceOf(IllegalStateException.class));
      checkpoint.flag();
    }));
    multiFuture.setHandler(context.failing(v -> {
      context.verify(() -> assertThat(v).isInstanceOf(IllegalStateException.class));
      checkpoint.flag();
    }));
  }

  @Test
  public void testCompletedFuture(VertxTestContext context) {
    Vertx vertx = Vertx.vertx();
    Future<String> fut = Future.future();
    ObservableFuture<String> multiFuture = ObservableFuture.wrap(fut);

    Checkpoint checkpoint = context.checkpoint(2);

    multiFuture.setHandler(context.succeeding(v -> {
      assertThat(v).isEqualTo("Hello");
      checkpoint.flag();
    }));
    multiFuture.setHandler(context.succeeding(v -> {
      assertThat(v).isEqualTo("Hello");
      checkpoint.flag();
    }));

    vertx.setTimer(1, l -> fut.complete("Hello"));
  }

  @Test
  public void testFailedFuture(VertxTestContext context) {
    Vertx vertx = Vertx.vertx();
    Future<String> fut = Future.future();
    ObservableFuture<String> multiFuture = ObservableFuture.wrap(fut);

    Checkpoint checkpoint = context.checkpoint(2);

    multiFuture.setHandler(context.failing(v -> {
      assertThat(v).isInstanceOf(IllegalStateException.class).hasMessage("Hello");
      checkpoint.flag();
    }));
    multiFuture.setHandler(context.failing(v -> {
      assertThat(v).isInstanceOf(IllegalStateException.class).hasMessage("Hello");
      checkpoint.flag();
    }));

    vertx.setTimer(1, l -> fut.fail(new IllegalStateException("Hello")));
  }

}
