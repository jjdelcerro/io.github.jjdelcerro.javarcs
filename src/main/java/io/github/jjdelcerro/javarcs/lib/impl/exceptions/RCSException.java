package io.github.jjdelcerro.javarcs.lib.impl.exceptions;

public class RCSException extends RuntimeException {
    public RCSException(String message) {
        super(message);
    }

    public RCSException(String message, Throwable cause) {
        super(message, cause);
    }
}
