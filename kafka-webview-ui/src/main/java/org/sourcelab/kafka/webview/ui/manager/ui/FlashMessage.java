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

package org.sourcelab.kafka.webview.ui.manager.ui;

import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ApiErrorCause;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ApiErrorResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an Alert Message.
 */
public class FlashMessage {
    private final String type;
    private final String message;
    private final List<String> details;

    private FlashMessage(final String type, final String message) {
        this(type, message, Collections.emptyList());
    }

    private FlashMessage(final String type, final String message, final List<String> details) {
        this.type = Objects.requireNonNull(type);
        this.message = Objects.requireNonNull(message);
        this.details = Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(details)));
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isWarning() {
        return "warning".equals(getType());
    }

    public boolean isInfo() {
        return "info".equals(getType());
    }

    public boolean isSuccess() {
        return "success".equals(getType());
    }

    public boolean isDanger() {
        return "danger".equals(getType());
    }

    public boolean hasDetails() {
        return !details.isEmpty();
    }

    public List<String> getDetails() {
        return details;
    }

    public static FlashMessage newSuccess(final String message) {
        return new FlashMessage("success", message);
    }

    public static FlashMessage newInfo(final String message) {
        return new FlashMessage("info", message);
    }

    public static FlashMessage newWarning(final String message) {
        return new FlashMessage("warning", message);
    }

    public static FlashMessage newDanger(final String message) {
        return new FlashMessage("danger", message);
    }

    /**
     * Create a new Danger alert with stack trace.
     * @param message Message to display.
     * @param details Additional details about the error.
     * @return FlashMessage instance.
     */
    public static FlashMessage newDanger(final String message, final List<String> details) {
        return new FlashMessage("danger", message, details);
    }

    /**
     * Create a new Danger alert with stack trace.
     * @param message Message to display.
     * @param cause Underlying exception.
     * @return FlashMessage instance.
     */
    public static FlashMessage newDanger(final String message, final Throwable cause) {
        final List<String> reasons = convertExceptionToDetails(cause);

        return new FlashMessage("danger", message, reasons);
    }

    private static List<String> convertExceptionToDetails(final Throwable throwable) {
        final ApiErrorCause[] causes = ApiErrorResponse.buildCauseList(throwable);
        final List<String> reasons = new ArrayList<>();

        for (final ApiErrorCause cause : causes) {
            reasons.add(cause.getType() + " thrown at " + cause.getMethod() + " -> " + cause.getMessage());
        }
        return reasons;
    }

    @Override
    public String toString() {
        return "FlashMessage{"
            + "type='" + type + '\''
            + ", message='" + message + '\''
            + '}';
    }
}
