package org.jpromise;

import java.util.Collections;
import java.util.List;

public class AggregatedException extends Exception {

    private final List<Exception> errors;

    public AggregatedException(List<Exception> errors) {
        this.errors = Collections.unmodifiableList(errors);
    }

    public List<Exception> getErrors() {
        return errors;
    }
}
