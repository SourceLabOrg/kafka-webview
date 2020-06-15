package org.sourcelab.kafka.webview.ui.manager.datatable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Defines a filter for a datatable.
 */
public class DatatableFilter {
    private final String label;
    private final String field;
    private final List<FilterOption> options;

    public DatatableFilter(final String label, final String field, final List<FilterOption> options) {
        this.label = Objects.requireNonNull(label);
        this.field = Objects.requireNonNull(field);

        // Sort options
        this.options = Objects.requireNonNull(options).stream()
            .sorted(Comparator.comparing(option -> option.getLabel().toLowerCase()))
            .collect(Collectors.toList());
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

    /**
     * Defines an option within a filter.
     */
    public static class FilterOption {
        private final String value;
        private final String label;

        /**
         * Constructor.
         * @param value Unique identifier for value.
         * @param label Display value.
         */
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
