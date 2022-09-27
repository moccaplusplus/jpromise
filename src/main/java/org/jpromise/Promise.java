package org.jpromise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Promise<T> implements Future<T> {

    private static final ExecutorService THREAD_POOL = ForkJoinPool.commonPool();

    public static <T> Promise<T> resolve(T value) {
        return new Promise<>(FutureResult.success(value));
    }

    public static <T> Promise<T> reject(Exception e) {
        return new Promise<T>(FutureResult.failure(e));
    }

    public static <T> Promise<T> wrap(Future<T> future) {
        return future instanceof Promise<T> promise ? promise : new Promise<>(future);
    }

    @SafeVarargs
    static <T> Promise<List<T>> all(Promise<? extends T>... promises) {
        return all(List.of(promises));
    }

    static <T> Promise<List<T>> all(Iterable<Promise<? extends T>> promises) {
        return new Promise<>(resolver -> {
            for (var p : promises) {
                p.doCatch(e -> {
                    resolver.reject(e);
                    return null;
                });
            }
            var result = new ArrayList<T>();
            for (var p : promises) result.add(p.get());
            resolver.resolve(result);
        });
    }

    @SafeVarargs
    static <T> Promise<List<SettledResult<T>>> allSettled(Promise<? extends T>... promises) {
        return allSettled(List.of(promises));
    }

    static <T> Promise<List<SettledResult<T>>> allSettled(Iterable<Promise<? extends T>> promises) {
        return new Promise<>(() -> {
            var result = new ArrayList<SettledResult<T>>();
            for (var p : promises) {
                try {
                    var value = p.get();
                    result.add(SettledResult.fulfilled(value));
                } catch (Exception e) {
                    result.add(SettledResult.rejected(e));
                }
            }
            return result;
        });
    }

    @SafeVarargs
    static <T> Promise<T> any(Promise<? extends T>... promises) {
        return any(List.of(promises));
    }

    static <T> Promise<T> any(Iterable<Promise<? extends T>> promises) {
        return new Promise<>(resolver -> {
            for (var p : promises) {
                p.then(result -> {
                    resolver.resolve(result);
                    return null;
                });
            }
            var errors = new ArrayList<Exception>();
            for (var p : promises) {
                try {
                    p.get();
                } catch (Exception e) {
                    errors.add(e);
                }
            }
            resolver.reject(new AggregatedException(errors));
        });
    }

    @SafeVarargs
    static <T> Promise<T> race(Promise<? extends T>... promises) {
        return race(List.of(promises));
    }

    static <T> Promise<T> race(Iterable<Promise<? extends T>> promises) {
        return new Promise<>(resolver -> {
            for (var p : promises) {
                p.then(result -> {
                    resolver.resolve(result);
                    return null;
                }, e -> {
                    resolver.reject(e);
                    return null;
                });
            }
        });
    }

    private final Future<T> future;

    public Promise(PromiseInit<T> promiseInit) {
        this(new PromiseResolverCallable<T>(promiseInit));
    }

    public Promise(Callable<T> callable) {
        this(THREAD_POOL.submit(callable));
    }

    Promise(Future<T> future) {
        this.future = future;
    }

    public <R> Promise<R> then(PromiseHandler<T, R> resultHandler) {
        return new Promise<>(() -> resultHandler.accept(get()));
    }

    public <R> Promise<R> then(PromiseHandler<T, R> resultHandler, PromiseHandler<Exception, R> errorHandler) {
        return new Promise<>(() -> {
            try {
                return resultHandler.accept(get());
            } catch (Exception e) {
                return errorHandler.accept(e);
            }
        });
    }

    public Promise<T> doCatch(PromiseHandler<Exception, T> errorHandler) {
        return new Promise<>(() -> {
            try {
                return get();
            } catch (Exception e) {
                return errorHandler.accept(e);
            }
        });
    }

    public Promise<T> doFinally(Runnable runnable) {
        return new Promise<>(() -> {
            T value = null;
            Exception error = null;
            try {
                value = get();
            } catch (Exception e) {
                error = e;
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
}
