package com.darksci.kafka.webview.ui.manager.plugin.exception;

/**
 * Thrown if the loaded class does not implement the correct interface.
 */
public class WrongImplementationException extends LoaderException {
    /**
     * Constructor.
     */
    public WrongImplementationException(final String message, final Exception cause) {
        super(message, cause);
    }
}
