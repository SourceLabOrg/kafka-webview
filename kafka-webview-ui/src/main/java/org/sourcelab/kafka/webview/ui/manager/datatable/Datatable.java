package org.sourcelab.kafka.webview.ui.manager.datatable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class Datatable<T> {
    private final Map<String, String> requestParams;
    private final String url;
    private final String label;
    private final Page<T> page;
    private final List<DatatableColumn> columns;
    private final List<DatatableFilter> filters;
    private final DatatableSearch datatableSearch;

    public Datatable(final Map<String, String> requestParams, final String url, final String label, final Page<T> page, final List<DatatableColumn> columns, final List<DatatableFilter> filters, final DatatableSearch datatableSearch) {
        this.requestParams = Collections.unmodifiableMap(new HashMap<>(requestParams));
        this.url = url;
        this.label = label;
        this.page = page;
        this.columns = columns;
        this.filters = filters;
        this.datatableSearch = datatableSearch;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlWithParams(final String ... overrides) {
        final Map<String, String> overrideParams = new HashMap<>();
        for (int index = 0; index < overrides.length; index = index + 2) {
            final String key = overrides[index];
            String value = "";
            if (index + 1 < overrides.length) {
                value = overrides[index + 1];
            }
            overrideParams.put(key, value);
        }

        final Map<String, String> params = new LinkedHashMap<>();

        // Add search
        if (getSearch() != null) {
            params.put("search", getSearch().getCurrentSearchTerm());
        }

        // Add filters
        getFilters().forEach((filter) -> params.put(filter.getField(), getCurrentFilterValueFor(filter)));

        if (page.getSort().isSorted()) {
            final List<String> sortValues = new ArrayList<>();
            page.getSort().get().forEachOrdered((sort) -> {
                sortValues.add(sort.getProperty() + "," + sort.getDirection());
            });
            params.put("sort", String.join(",", sortValues));
        }
        params.put("page", Integer.toString(getNumber()));

        final List<String> paramStr = new ArrayList<>();
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            // Allow for injecting/overriding specific params.
            String key = entry.getKey();
            String value = entry.getValue();
            if (overrideParams.containsKey(key)) {
                value = overrideParams.get(key);
            }
            // Skip if empty
            if (value == null || value.isEmpty()) {
                continue;
            }
            // TODO URL Encode params
            paramStr.add(key + "=" + value);
        }

        return getUrl() + "?" + String.join("&", paramStr);
    }

    public String getCurrentFilterValueFor(final DatatableFilter filter) {
        final String param = filter.getField();
        if (requestParams.containsKey(param)) {
            return requestParams.get(param);
        }
        return "";
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

    public String getCurrentSortOrderFor(final DatatableColumn column) {
        final String defaultOrder = "desc";
        if (!column.isSortable()) {
            return defaultOrder;
        }
        final Sort.Order order = page.getSort().getOrderFor(column.getField());
        if (order == null || order.isDescending()) {
            return defaultOrder;
        }
        return "asc";
    }

    public boolean isSortedBy(final DatatableColumn column) {
        if (!column.isSortable()) {
            return false;
        }
        final Sort.Order order = page.getSort().getOrderFor(column.getField());
        if (order == null) {
            return false;
        }
        return true;
    }

    public String getInverseSortOrderFor(final DatatableColumn column) {
        // If not currently sorted by this column.
        if (!isSortedBy(column)) {
            // Default to descending.
            return "desc";
        }
        // Otherwise invert the value.
        final String order = getCurrentSortOrderFor(column);
        if ("desc".equalsIgnoreCase(order)) {
            return "asc";
        }
        return "desc";
    }

    public static <T> Builder<T> newBuilder(Class<T> type) {
        return new Builder<T>();
    }

    public static final class Builder<T> {
        private Map<String, String> requestParams = new HashMap<>();
        private String url;
        private String label;
        private Page<T> page;
        private List<DatatableColumn> columns = new ArrayList<>();
        private List<DatatableFilter> filters = new ArrayList<>();
        private DatatableSearch datatableSearch;

        private Builder() {
        }

        public Builder<T> withRequestParams(final Map<String, String> requestParams) {
            this.requestParams.clear();
            this.requestParams.putAll(requestParams);
            return this;
        }

        public Builder<T> withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder<T> withUrl(String url) {
            this.url = url;
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

        public Builder<T> withSearch(final String search, final String name, final String currentSearchTerm) {
            return withSearch(new DatatableSearch(search, name, currentSearchTerm));
        }

        public Builder<T> withPage(final Page<T> page) {
            this.page = page;
            return this;
        }

        public Datatable<T> build() {
            // Inject current search term from request parameters if available and not already set.
            if (datatableSearch != null && datatableSearch.getField() != null && datatableSearch.getCurrentSearchTerm() == null) {
                if (requestParams.containsKey("search")) {
                    datatableSearch = new DatatableSearch(
                        datatableSearch.getLabel(),
                        datatableSearch.getField(),
                        requestParams.get("search")
                    );
                }
            }

            return new Datatable<>(requestParams, url, label, page, columns, filters, datatableSearch);
        }
    }
}
