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

package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Abstract base RenderTemplate for rendering fields on a datatable.
 */
abstract class RenderTemplate<T> {
    private final String templateFile;
    private final String functionName;

    /**
     * Constructor.
     *
     * Example parameters:
     *  templateFile: "templates/fragments/SomeFile"
     *  functionName: "display" to call the display function defined in that template.
     *
     * @param templateFile path to the template to render.
     * @param functionName function in the template to render.
     */
    public RenderTemplate(final String templateFile, final String functionName) {
        this.templateFile = Objects.requireNonNull(templateFile);
        this.functionName = Objects.requireNonNull(functionName);
    }

    /**
     * Abstract method.
     * Defines the parameters to be passed to the template function as parameters.
     * @param record the record to be displayed.
     * @return List of values to be passed to the function.
     */
    abstract List<Object> getParameters(T record);

    public String getTemplateFile() {
        return templateFile;
    }

    public String getFunctionName() {
        return functionName;
    }

    /**
     * Render the parameters as a string.
     * @param record The record to be displayed.
     * @return The parameters deliminated by commas.
     */
    public String getParametersStr(T record) {
        final List<Object> params = getParameters(record);
        if (params.isEmpty()) {
            return "";
        }
        return params.stream()
            .map((param) -> {
                if (param instanceof Boolean || param instanceof Number) {
                    return "" + param;
                }
                return "'" + param + "'";
            })
            .collect(Collectors.joining(", "));
    }

    /**
     * The thymeleaf template call with parameters injected.
     * @param record The record to be displayed.
     * @return function call string.
     */
    public String getTemplateCall(T record) {
        return getTemplateFile() + " :: " + getFunctionName() + "(" + getParametersStr(record) + ")";
    }
}
