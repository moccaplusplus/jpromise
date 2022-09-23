package org.jpromise;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

class PromiseResolverCallable<T> implements PromiseResolver<T>, Callable<T> {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final PromiseInit<T> promiseInit;
    private T value;
    private Exception error;

    public PromiseResolverCallable(PromiseInit<T> promiseInit) {
        this.promiseInit = promiseInit;
    }

    @Override
    public void resolve(T value) {
        this.value = value;
        latch.countDown();
    }

    @Override
    public void reject(Exception error) {
        this.error = error;
        latch.countDown();
    }

    @Override
    public T call() throws Exception {
        promiseInit.accept(this);
        latch.await();
        if (error != null) throw error;
        return value;
    }
}
