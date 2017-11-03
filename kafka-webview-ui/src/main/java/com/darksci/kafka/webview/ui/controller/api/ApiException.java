package com.darksci.kafka.webview.ui.controller.api;

/**
 * Represents an error that occurred within an API request.
 */
public class ApiException extends RuntimeException {
    private final String type;

    /**
     * Constructor.
     */
    public ApiException(final String type, final Throwable cause) {
        super(cause.getMessage(), cause);
        this.type = type;
    }

    /**
     * Constructor.
     */
    ApiException(final String type, final String message) {
        super(message);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
