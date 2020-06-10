package org.sourcelab.kafka.webview.ui.manager.datatable;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
abstract class RenderTemplate<T> {
    private final String templateFile;
    private final String functionName;

    public RenderTemplate(final String templateFile, final String functionName) {
        this.templateFile = templateFile;
        this.functionName = functionName;
    }

    public String getTemplateFile() {
        return templateFile;
    }

    abstract List<Object> getParameters(T record);

    public String getParametersStr(T record) {
        final List<Object> params = getParameters(record);
        if (params.isEmpty()) {
            return "";
        }
        return params.stream()
            .map((param) -> "'" + param + "'")
            .collect(Collectors.joining(", "));
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getTemplateCall(T record) {
        return getTemplateFile() + " :: " + getFunctionName() + "(" + getParametersStr(record) + ")";
    }
}
