package org.jpromise;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class RunningPromise<T> implements Promise<T> {

    private final ExecutorService threadPool;
    private final Future<T> future;

    RunningPromise(ExecutorService threadPool, Callable<T> callable) {
        this.threadPool = threadPool;
        future = threadPool.submit(callable);
    }

    @Override
    public <R> Promise<R> then(PromiseHandler<T, R> resultHandler) {
        return new RunningPromise<>(threadPool, () -> resultHandler.accept(get()));
    }

    @Override
    public <R> Promise<R> then(PromiseHandler<T, R> resultHandler, PromiseHandler<Exception, R> errorHandler) {
        return new RunningPromise<>(threadPool, () -> {
            try {
                return resultHandler.accept(future.get());
            } catch (Exception e) {
                return errorHandler.accept(getError(e));
            }
        });
    }

    @Override
    public Promise<T> doCatch(PromiseHandler<Exception, T> errorHandler) {
        return new RunningPromise<>(threadPool, () -> {
            try {
                return future.get();
            } catch (Exception e) {
                return errorHandler.accept(getError(e));
            }
        });
    }

    public Promise<T> doFinally(Runnable runnable) {
        return new RunningPromise<>(threadPool, () -> {
            T value = null;
            Exception error = null;
            try {
                value = future.get();
            } catch (Exception e) {
                error = getError(e);
            }
            runnable.run();
            if (error != null) throw error;
            return value;
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public T get() throws ExecutionException, InterruptedException {
        return future.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    private Exception getError(Exception e) {
        return e instanceof ExecutionException ee && ee.getCause() instanceof Exception cause ? cause : e;
    }
}
