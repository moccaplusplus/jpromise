package org.jpromise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@FunctionalInterface
interface FutureResult<T> extends Future<T> {

    static <T> FutureResult<T> success(T value) {
        return () -> value;
    }

    static <T> FutureResult<T> failure(Exception reason) {
        return () -> {
            throw new ExecutionException(reason);
        };
    }

    @Override
    default boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    default boolean isCancelled() {
        return false;
    }

    @Override
    default boolean isDone() {
        return true;
    }

    @Override
    default T get(long timeout, TimeUnit unit) throws ExecutionException {
        return get();
    }

    @Override
    T get() throws ExecutionException;
}
