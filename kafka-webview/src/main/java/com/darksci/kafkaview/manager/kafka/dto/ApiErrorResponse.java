package com.darksci.kafkaview.manager.kafka.dto;

public class ApiErrorResponse {
    private final boolean error = true;
    private final String errorMessage;
    private final String requestType;

    public ApiErrorResponse(final String requestType, final String errorMessage) {
        this.errorMessage = errorMessage;
        this.requestType = requestType;
    }

    public boolean isError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getRequestType() {
        return requestType;
    }
}
