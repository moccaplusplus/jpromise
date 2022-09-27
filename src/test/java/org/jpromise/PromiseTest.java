package org.jpromise;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

class PromiseTest {

    @Test
    void testPromise() throws InterruptedException {
        // given
        var latch = new CountDownLatch(1);
        var result = new AtomicInteger();
        var promise = new Promise<Integer>(resolver -> {
            Thread.sleep(500);
            resolver.resolve(5);
        });

        // when
        promise
                .then(i -> {
                    result.set(i);
                    return i;
                })
                .doCatch(e -> {
                    result.set(-1);
                    return null;
                })
                .doFinally(latch::countDown);
        result.set(3);

        // then
        latch.await();
        Assertions.assertEquals(5, result.get());
    }
}