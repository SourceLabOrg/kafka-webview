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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Defines a column within a datatable.
 * @param <T> Type of object being rendered by the column.
 */
public class DatatableColumn<T> {
    private final String fieldName;
    private final String label;
    private final int colSpan;
    private final boolean isSortable;
    private final List<String> headerCssClasses;
    private Function<T, String> renderFunction;
    private final RenderTemplate renderTemplate;

    /**
     * Constructor.
     * See Builder instance.
     *
     * @param fieldName The underlying field name to be displayed in the column.
     * @param label Display label for the column.
     * @param colSpan How many columns should the field span.
     * @param isSortable Is the column sortable.
     * @param renderFunction A callable function that takes in the current record and returns the value to be displayed.
     * @param renderTemplate A callabale thymeleaf template.
     */
    public DatatableColumn(
        final String fieldName, final String label, final int colSpan, final boolean isSortable,
        final List<String> headerCssClasses,
        final Function<T, String> renderFunction,
        final RenderTemplate renderTemplate
    ) {
        this.fieldName = Objects.requireNonNull(fieldName);
        this.label = Objects.requireNonNull(label);
        this.colSpan = colSpan;
        this.isSortable = isSortable;
        this.headerCssClasses = Collections.unmodifiableList(new ArrayList<>(headerCssClasses));

        // One of these may not be null.
        this.renderFunction = renderFunction;
        this.renderTemplate = renderTemplate;
    }

    /**
     * The RenderTemplate instance used to render the columns values.
     * If no specific instance was provided to the constructor, the default render template
     * will be created and used.
     *
     * @return RenderTemplate.
     */
    public RenderTemplate getRenderTemplate() {
        if (renderTemplate == null) {
            return new RenderTemplate<T>("fragments/datatable/fields/TextValue", "display") {
                @Override
                List<Object> getParameters(final T record) {
                    return Collections.singletonList(render(record));
                }
            };
        }
        return renderTemplate;
    }

    /**
     * Render the current columns value for the given record.
     * @param record The record to render the column field value for.
     * @return Rendered field value.
     */
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

    public String getHeaderCssClasses() {
        return String.join(" ", headerCssClasses).trim();
    }

    /**
     * Create a new Builder instance.
     * @param type The type of record being rendered.
     * @param <T> The type of record being rendered.
     * @return New Builder instance.
     */
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

    /**
     * Builder instance for DatatableColumn.
     * @param <T> Type of object being rendered in the column.
     */
    public static final class Builder<T> {
        private String fieldName;
        private String label;
        private int colSpan = 1;
        private boolean isSortable = true;
        private Function<T, String> renderFunction = null;
        private RenderTemplate<T> renderTemplate = null;
        private List<String> headerCssClasses = new ArrayList<>();

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

        public Builder<T> withHeaderAlignRight() {
            this.headerCssClasses.add("text-right");
            return this;
        }

        public Builder<T> withRenderFunction(Function<T, String> renderFunction) {
            this.renderFunction = renderFunction;
            return this;
        }

        public Builder<T> withRenderTemplate(final RenderTemplate<T> renderTemplate) {
            this.renderTemplate = renderTemplate;
            return this;
        }

        /**
         * Create new DatatableColumn from builder.
         * @return new DatatableColumn.
         */
        public DatatableColumn<T> build() {
            return new DatatableColumn<T>(fieldName, label, colSpan, isSortable, headerCssClasses, renderFunction, renderTemplate);
        }
    }
}
