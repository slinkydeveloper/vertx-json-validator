package io.vertx.ext.json.schema.generic;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ObservableFuture<T> implements Future<T> {

  final Future<T> delegate;
  final Queue<Handler<AsyncResult<T>>> handlers;

  private ObservableFuture(Future<T> delegate) {
    this.handlers = new ConcurrentLinkedQueue<>();
    this.delegate = delegate;
    this.delegate.setHandler(ar -> {
      while (!this.handlers.isEmpty()) {
        this.handlers.poll().handle(this);
      }
    });
  }

  @Override
  public boolean isComplete() {
    return delegate.isComplete();
  }

  @Override
  public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
    if (delegate.isComplete())
      handler.handle(this);
    else
      handlers.add(handler);
    return this;
  }

  @Override
  public Handler<AsyncResult<T>> getHandler() {
    return handlers.peek();
  }

  @Override
  public void complete(T result) {
    delegate.complete(result);
  }

  @Override
  public void complete() {
    delegate.complete();
  }

  @Override
  public void fail(Throwable cause) {
    delegate.fail(cause);
  }

  @Override
  public void fail(String failureMessage) {
    delegate.fail(failureMessage);
  }

  @Override
  public boolean tryComplete(T result) {
    return delegate.tryComplete(result);
  }

  @Override
  public boolean tryComplete() {
    return delegate.tryComplete();
  }

  @Override
  public boolean tryFail(Throwable cause) {
    return delegate.tryFail(cause);
  }

  @Override
  public boolean tryFail(String failureMessage) {
    return delegate.tryFail(failureMessage);
  }

  @Override
  public T result() {
    return delegate.result();
  }

  @Override
  public Throwable cause() {
    return delegate.cause();
  }

  @Override
  public boolean succeeded() {
    return delegate.succeeded();
  }

  @Override
  public boolean failed() {
    return delegate.failed();
  }

  @Override
  public void handle(AsyncResult<T> asyncResult) {
    delegate.handle(asyncResult);
  }

  public static <T> ObservableFuture<T> wrap(Future<T> fut) {
    return new ObservableFuture<>(fut);
  }

}
