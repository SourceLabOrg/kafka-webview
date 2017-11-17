package org.sourcelab.kafka.webview.ui.manager.plugin.exception;

/**
 * Thrown if unable to load the specified Class from the given Jar.
 */
public class UnableToFindClassException extends LoaderException {
    /**
     * Constructor.
     */
    public UnableToFindClassException(final String message, final Exception cause) {
        super(message, cause);
    }
}
