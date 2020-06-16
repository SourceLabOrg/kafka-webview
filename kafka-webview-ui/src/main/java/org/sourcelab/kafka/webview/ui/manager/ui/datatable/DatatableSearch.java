package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

/**
 *
 */
public class DatatableSearch {
    private final String label;
    private final String field;
    private final String currentSearchTerm;

    /**
     * Constructor.
     * @param label Human readable display label.
     * @param field The underlying field to search over.
     * @param currentSearchTerm The current search term if defined in a request.
     */
    public DatatableSearch(final String label, final String field, final String currentSearchTerm) {
        this.label = label;
        this.field = field;
        this.currentSearchTerm = currentSearchTerm;
    }

    public String getLabel() {
        return label;
    }

    public String getField() {
        return field;
    }

    public String getCurrentSearchTerm() {
        return currentSearchTerm;
    }

    @Override
    public String toString() {
        return "DatatableSearch{"
            + "label='" + label + '\''
            + ", field='" + field + '\''
            + '}';
    }
}
