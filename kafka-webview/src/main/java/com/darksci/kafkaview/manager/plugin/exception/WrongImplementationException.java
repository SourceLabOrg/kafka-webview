package com.darksci.kafkaview.manager.plugin.exception;

public class WrongImplementationException extends LoaderException {
    public WrongImplementationException(final String message, final Exception cause) {
        super(message, cause);
    }
}
