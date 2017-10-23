package com.darksci.kafka.webview.ui.controller.api;

public class NotFoundApiException extends ApiException {

    public NotFoundApiException(final String type, final String message) {
        super(type, message);
    }
}
