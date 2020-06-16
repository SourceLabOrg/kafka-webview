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
            .map((param) -> "'" + param + "'")
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
