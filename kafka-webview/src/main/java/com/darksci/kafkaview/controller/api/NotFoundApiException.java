package com.darksci.kafkaview.controller.api;

public class NotFoundApiException extends ApiException {

    public NotFoundApiException(final String type, final String message) {
        super(type, message);
    }
}
