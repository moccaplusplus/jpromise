package org.jpromise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class RejectedPromise<T> implements Promise<T> {

    private final Exception error;

    RejectedPromise(Exception error) {
        this.error = error;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Promise<R> then(PromiseHandler<T, R> resultHandler) {
        return (Promise<R>) this;
    }

    @Override
    public <R> Promise<R> then(PromiseHandler<T, R> resultHandler, PromiseHandler<Exception, R> errorHandler) {
        return Promise.of(() -> errorHandler.accept(error));
    }

    @Override
    public Promise<T> doCatch(PromiseHandler<Exception, T> errorHandler) {
        return Promise.of(() -> errorHandler.accept(error));
    }

    @Override
    public Promise<T> doFinally(Runnable runnable) {
        return Promise.of(() -> {
            runnable.run();
            throw error;
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
        return false;
    }

    @Override
    public T get() throws ExecutionException {
        throw new ExecutionException(error);
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws ExecutionException {
        return get();
    }
}
