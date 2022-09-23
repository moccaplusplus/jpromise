package org.jpromise;

public interface PromiseResolver<T> {
    void resolve(T value);

    void reject(Exception e);
}
