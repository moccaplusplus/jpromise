package org.jpromise;

@FunctionalInterface
public interface PromiseHandler<T, R> {
    R accept(T result) throws Exception;
}
