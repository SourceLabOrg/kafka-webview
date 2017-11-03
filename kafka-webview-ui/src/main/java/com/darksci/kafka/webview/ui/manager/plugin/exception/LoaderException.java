package com.darksci.kafka.webview.ui.manager.plugin.exception;

/**
 * Thrown if an error occurs while attempting to load a Plugin Class.
 */
public class LoaderException extends Exception {
    /**
     * Constructor.
     */
    public LoaderException(final String message, final Exception cause) {
        super(message, cause);
    }

}
