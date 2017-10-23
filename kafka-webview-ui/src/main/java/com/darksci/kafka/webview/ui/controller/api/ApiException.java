package com.darksci.kafka.webview.ui.controller.api;

public class ApiException extends RuntimeException {
    private final String type;

    public ApiException(final String type, final Throwable cause) {
        super(cause.getMessage(), cause);
        this.type = type;
    }

    ApiException(final String type, final String message) {
        super(message);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
