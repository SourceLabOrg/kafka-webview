package com.darksci.kafka.webview.manager.plugin.exception;

public class WrongImplementationException extends LoaderException {
    public WrongImplementationException(final String message, final Exception cause) {
        super(message, cause);
    }
}
