package com.darksci.kafka.webview.ui.manager.kafka.dto;

public class ApiErrorResponse {
    private final boolean error = true;
    private final String message;
    private final String requestType;

    public ApiErrorResponse(final String requestType, final String message) {
        this.message = message;
        this.requestType = requestType;
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestType() {
        return requestType;
    }
}
