package com.darksci.kafka.webview.ui.controller.api;

/**
 * Error when requesting an object over the API that doesn't not exist.
 */
public class NotFoundApiException extends ApiException {

    /**
     * Constructor.
     */
    public NotFoundApiException(final String type, final String message) {
        super(type, message);
    }
}
