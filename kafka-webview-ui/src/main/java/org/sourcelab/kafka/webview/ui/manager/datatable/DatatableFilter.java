package org.sourcelab.kafka.webview.ui.manager.datatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class DatatableFilter {
    private final String label;
    private final String field;
    private final List<FilterOption> options;

    public DatatableFilter(final String label, final String field, final List<FilterOption> options) {
        this.label = Objects.requireNonNull(label);
        this.field = Objects.requireNonNull(field);
        this.options = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(options)));
    }

    public String getLabel() {
        return label;
    }

    public String getField() {
        return field;
    }

    public List<FilterOption> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "DatatableFilter{"
            + "label='" + label + '\''
            + ", field='" + field + '\''
            + ", options=" + options
            + '}';
    }

    public static class FilterOption {
        private final String value;
        private final String label;

        public FilterOption(final String value, final String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return "FilterOption{"
                + "value='" + value + '\''
                + ", label='" + label + '\''
                + '}';
        }
    }
}
