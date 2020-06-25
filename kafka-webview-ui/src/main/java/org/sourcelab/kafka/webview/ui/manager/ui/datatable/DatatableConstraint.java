package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

import java.util.Objects;

/**
 * Enforced constraint on datatable query.
 */
public class DatatableConstraint {
    private final String field;
    private final Object value;
    private final ConstraintOperator operator;

    public DatatableConstraint(final String field, final Object value, final ConstraintOperator operator) {
        this.field = Objects.requireNonNull(field);
        this.value = Objects.requireNonNull(value);
        this.operator = Objects.requireNonNull(operator);
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public ConstraintOperator getOperator() {
        return operator;
    }
}
