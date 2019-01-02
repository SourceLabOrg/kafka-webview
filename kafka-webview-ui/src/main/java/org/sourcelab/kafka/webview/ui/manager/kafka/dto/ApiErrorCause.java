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

package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

/**
 * Underlying causes for Api Error.
 */
public class ApiErrorCause {
    private final String type;
    private final String message;
    private final String file;
    private final String method;
    private final int line;

    /**
     * Constructor.
     * @param type Classname of exception.
     * @param message Error msg.
     * @param file File the exception was generated from.
     * @param method Method the exception was generated from.
     * @param line line number the exception was generated from.
     */
    public ApiErrorCause(final String type, final String message, final String file, final String method, final int line) {
        this.type = type;
        this.message = message;
        this.file = file;
        this.method = method;
        this.line = line;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getFile() {
        return file;
    }

    public String getMethod() {
        return method;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "ApiErrorCause{"
            + "type='" + type + '\''
            + ", message='" + message + '\''
            + ", file='" + file + '\''
            + ", method='" + method + '\''
            + ", line=" + line
            + '}';
    }
}
