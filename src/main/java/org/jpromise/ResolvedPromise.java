package org.jpromise;

import java.util.concurrent.TimeUnit;

class ResolvedPromise<T> implements Promise<T> {

    private final T value;

    ResolvedPromise(T value) {
        this.value = value;
    }

    @Override
    public <R> Promise<R> then(PromiseHandler<T, R> resultHandler) {
        return Promise.of(() -> resultHandler.accept(value));
    }

    @Override
    public <R> Promise<R> then(PromiseHandler<T, R> resultHandler, PromiseHandler<Exception, R> errorHandler) {
        return then(resultHandler);
    }

    @Override
    public Promise<T> doCatch(PromiseHandler<Exception, T> errorHandler) {
        return this;
    }

    @Override
    public Promise<T> doFinally(Runnable runnable) {
        return Promise.of(() -> {
            runnable.run();
            return value;
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return value;
    }
}
