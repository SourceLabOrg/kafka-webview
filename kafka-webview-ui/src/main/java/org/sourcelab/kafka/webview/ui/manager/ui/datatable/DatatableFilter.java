/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import java.util.ArrayList;
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

    /**
     * Constructor.
     * @param label Human readable display label for the filter.
     * @param field The underlying field being filtered over.
     * @param options Options to display in the filter.
     */
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

    /**
     * New Builder instance.
     * @return Builder instance.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builder instance.
     */
    public static final class Builder {
        private String label;
        private String field;
        private List<FilterOption> options = new ArrayList<>();

        private Builder() {
        }


        public Builder withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder withField(String field) {
            this.field = field;
            return this;
        }

        /**
         * Add multiple options for the filter.
         * @param options Defined options to add.
         * @return Builder instance.
         */
        public Builder withOptions(final List<FilterOption> options) {
            Objects.requireNonNull(options);
            this.options.clear();
            this.options.addAll(options);
            return this;
        }

        public Builder withOption(final String value, final String label) {
            return withOption(new DatatableFilter.FilterOption(value, label));
        }

        public Builder withOption(final Number value, final String label) {
            return withOption(value.toString(), label);
        }

        public Builder withOption(final DatatableFilter.FilterOption option) {
            this.options.add(option);
            return this;
        }

        /**
         * Create new DatatableFilter.
         * @return DatatableFilter instance.
         */
        public DatatableFilter build() {
            return new DatatableFilter(label, field, options);
        }
    }
}
