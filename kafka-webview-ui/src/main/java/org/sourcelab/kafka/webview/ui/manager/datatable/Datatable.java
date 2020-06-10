package org.sourcelab.kafka.webview.ui.manager.datatable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.model.View;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Datatable<T> {
    private final String label;
    private final Page<T> page;
    private final List<DatatableColumn> columns;
    private final List<DatatableFilter> filters;
    private final DatatableSearch datatableSearch;

    public Datatable(final String label, final Page<T> page, final List<DatatableColumn> columns, final List<DatatableFilter> filters, final DatatableSearch datatableSearch) {
        this.label = label;
        this.page = page;
        this.columns = columns;
        this.filters = filters;
        this.datatableSearch = datatableSearch;
    }

    public String getLabel() {
        return label;
    }

    public long getTotalElements() {
        return page.getTotalElements();
    }

    public int getTotalPages() {
        return page.getTotalPages();
    }

    public int getNumber() {
        return page.getNumber();
    }

    public boolean isLastPage() {
        return page.isLast();
    }

    public boolean hasNextPage() {
        return page.hasNext();
    }

    public boolean hasPreviousPage() {
        return page.hasPrevious();
    }

    public boolean isFirstPage() {
        return page.isFirst();
    }

    public boolean isEmpty() {
        return page.isEmpty();
    }

    public List<T> getRecords() {
        return page.getContent();
    }

    public List<DatatableColumn> getColumns() {
        return columns;
    }

    public List<DatatableFilter> getFilters() {
        return filters;
    }

    public boolean hasFilters() {
        return filters != null && !filters.isEmpty();
    }

    public boolean hasSearch() {
        return datatableSearch != null;
    }

    public DatatableSearch getSearch() {
        return datatableSearch;
    }

    public static <T> Builder<T> newBuilder(Class<T> type) {
        return new Builder<T>();
    }

    public static final class Builder<T> {
        private String label;
        private Page<T> page;
        private List<DatatableColumn> columns = new ArrayList<>();
        private List<DatatableFilter> filters = new ArrayList<>();
        private DatatableSearch datatableSearch;

        private Builder() {
        }

        public Builder<T> withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder<T> withColumns(List<DatatableColumn> columns) {
            this.columns.addAll(columns);
            return this;
        }

        public Builder<T> withColumn(final DatatableColumn column) {
            this.columns.add(column);
            return this;
        }

        public Builder<T> withFilters(List<DatatableFilter> filters) {
            this.filters.addAll(filters);
            return this;
        }

        public Builder<T> withFilter(final DatatableFilter filter) {
            this.filters.add(filter);
            return this;
        }

        public Builder<T> withSearch(DatatableSearch datatableSearch) {
            this.datatableSearch = datatableSearch;
            return this;
        }

        public Builder<T> withSearch(final String search, final String name) {
            return withSearch(new DatatableSearch(search, name));
        }

        public Builder<T> withPage(final Page<T> page) {
            this.page = page;
            return this;
        }

        public Datatable<T> build() {
            return new Datatable<>(label, page, columns, filters, datatableSearch);
        }
    }
}
