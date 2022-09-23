package org.jpromise;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public interface Promise<T> extends Future<T> {

    ExecutorService DEFAULT_THREAD_POOL = ForkJoinPool.commonPool();

    static <T> Promise<T> of(PromiseInit<T> promiseInit) {
        return new RunningPromise<>(DEFAULT_THREAD_POOL, new PromiseResolverCallable<T>(promiseInit));
    }

    static <T> Promise<T> of(Callable<T> callable) {
        return new RunningPromise<>(DEFAULT_THREAD_POOL, callable);
    }

    static <T> Promise<T> of(ExecutorService threadPool, PromiseInit<T> promiseInit) {
        return new RunningPromise<>(threadPool, new PromiseResolverCallable<>(promiseInit));
    }

    static <T> Promise<T> of(ExecutorService threadPool, Callable<T> callable) {
        return new RunningPromise<>(threadPool, callable);
    }

    static <T> Promise<T> resolve(T value) {
        return new ResolvedPromise<>(value);
    }

    static <T> Promise<T> reject(Exception e) {
        return new RejectedPromise<>(e);
    }

    <R> Promise<R> then(PromiseHandler<T, R> resultHandler);

    <R> Promise<R> then(PromiseHandler<T, R> resultHandler, PromiseHandler<Exception, R> errorHandler);

    Promise<T> doCatch(PromiseHandler<Exception, T> errorHandler);

    Promise<T> doFinally(Runnable runnable);
}
