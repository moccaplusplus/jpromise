package org.jpromise;

@FunctionalInterface
public interface PromiseInit<T> {
    void accept(PromiseResolver<T> resolver) throws Exception;
}
