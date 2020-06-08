/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import net.bytebuddy.implementation.bytecode.Throw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an error returned over the API.
 */
public class ApiErrorResponse {
    private final boolean error = true;
    private final String message;
    private final String requestType;
    private final ApiErrorCause[] causes;

    /**
     * Constructor.
     */
    public ApiErrorResponse(final String requestType, final String message, final ApiErrorCause[] causes) {
        this.message = message;
        this.requestType = requestType;
        this.causes = causes;
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

    public ApiErrorCause[] getCauses() {
        return causes;
    }

    @Override
    public String toString() {
        return "ApiErrorResponse{"
            + "error=" + error
            + ", message='" + message + '\''
            + ", requestType='" + requestType + '\''
            + ", causes=" + Arrays.toString(causes)
            + '}';
    }

    /**
     * Utility method to generate underlying ApiErrorCause array from exception.
     * @param exception exception.
     * @return Array of ApiErrorCauses.
     */
    public static ApiErrorCause[] buildCauseList(final Throwable exception) {
        if (exception == null) {
            return new ApiErrorCause[0];
        }

        final List<ApiErrorCause> causeList = new ArrayList<>();
        Throwable cause = exception.getCause();
        while (cause != null) {
            final StackTraceElement[] trace = cause.getStackTrace();
            String file = "";
            String method = "";
            int line = 0;

            if (trace.length > 0) {
                file = trace[0].getFileName();
                method = trace[0].getClassName() + "::" + trace[0].getMethodName();
                line = trace[0].getLineNumber();
            }

            causeList.add(new ApiErrorCause(
                cause.getClass().getName(),
                cause.getMessage(),
                file,
                method,
                line
            ));

            // Continue loop.
            cause = cause.getCause();
        }
        return causeList.toArray(new ApiErrorCause[0]);
    }
}
