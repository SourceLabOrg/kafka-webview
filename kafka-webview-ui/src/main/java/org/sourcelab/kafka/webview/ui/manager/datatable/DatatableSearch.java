package org.sourcelab.kafka.webview.ui.manager.datatable;

/**
 *
 */
public class DatatableSearch {
    private final String label;
    private final String field;

    public DatatableSearch(final String label, final String field) {
        this.label = label;
        this.field = field;
    }

    public String getLabel() {
        return label;
    }

    public String getField() {
        return field;
    }

    @Override
    public String toString() {
        return "DatatableSearch{"
            + "label='" + label + '\''
            + ", field='" + field + '\''
            + '}';
    }
}
