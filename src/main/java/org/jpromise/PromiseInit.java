package org.jpromise;

@FunctionalInterface
public interface PromiseInit<T> {
    void init(PromiseResolver<T> resolver) throws Exception;
}
