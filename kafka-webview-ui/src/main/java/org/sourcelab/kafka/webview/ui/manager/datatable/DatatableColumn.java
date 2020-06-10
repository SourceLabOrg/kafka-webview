package org.sourcelab.kafka.webview.ui.manager.datatable;

import org.springframework.data.domain.Sort;

/**
 *
 */
public class DatatableColumn {
    private final String fieldName;
    private final String label;
    private final int colSpan;
    private final boolean isSortable;

    public DatatableColumn(final String fieldName, final String label) {
        this(fieldName, label, 1, false);
    }

    public DatatableColumn(final String fieldName, final String label, final int colSpan, final boolean isSortable) {
        this.fieldName = fieldName;
        this.label = label;
        this.colSpan = colSpan;
        this.isSortable = isSortable;
    }

    public String getField() {
        return fieldName;
    }

    public String getLabel() {
        return label;
    }

    public int getColSpan() {
        return colSpan;
    }

    public boolean isSortable() {
        return isSortable;
    }

    @Override
    public String toString() {
        return "DatatableColumn{"
            + "fieldName='" + fieldName + '\''
            + ", label='" + label + '\''
            + '}';
    }
}
