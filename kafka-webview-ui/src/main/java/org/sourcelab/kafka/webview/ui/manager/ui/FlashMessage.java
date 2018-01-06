/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

/**
 * Represents an Alert Message.
 */
public class FlashMessage {
    private final String type;
    private final String message;

    private FlashMessage(final String type, final String message) {
        this.type = type;
        this.message = message;
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

    @Override
    public String toString() {
        return "FlashMessage{"
            + "type='" + type + '\''
            + ", message='" + message + '\''
            + '}';
    }
}
