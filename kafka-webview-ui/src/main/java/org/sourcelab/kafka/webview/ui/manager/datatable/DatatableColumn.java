package org.sourcelab.kafka.webview.ui.manager.datatable;

import org.springframework.data.domain.Sort;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 */
public class DatatableColumn<T> {
    private final String fieldName;
    private final String label;
    private final int colSpan;
    private final boolean isSortable;
    private Function<T, String> renderFunction;

    public DatatableColumn(
        final String fieldName, final String label, final int colSpan, final boolean isSortable,
        final Function<T, String> renderFunction
    ) {
        this.fieldName = fieldName;
        this.label = label;
        this.colSpan = colSpan;
        this.isSortable = isSortable;
        this.renderFunction = renderFunction;
    }

    public String render(final T record) {
        return renderFunction.apply(record);
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

    public static <T> Builder<T> newBuilder(Class<T> type) {
        return new Builder<T>();
    }

    @Override
    public String toString() {
        return "DatatableColumn{"
            + "fieldName='" + fieldName + '\''
            + ", label='" + label + '\''
            + '}';
    }


    public static final class Builder<T> {
        private String fieldName;
        private String label;
        private int colSpan = 1;
        private boolean isSortable = true;
        private Function<T, String> renderFunction;

        private Builder() {
        }

        public Builder<T> withFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder<T> withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder<T> withColSpan(int colSpan) {
            this.colSpan = colSpan;
            return this;
        }

        public Builder<T> withIsSortable(boolean isSortable) {
            this.isSortable = isSortable;
            return this;
        }

        public Builder<T> withRenderFunction(Function<T, String> renderFunction) {
            this.renderFunction = renderFunction;
            return this;
        }

        public DatatableColumn<T> build() {
            return new DatatableColumn<T>(fieldName, label, colSpan, isSortable, renderFunction);
        }
    }
}
