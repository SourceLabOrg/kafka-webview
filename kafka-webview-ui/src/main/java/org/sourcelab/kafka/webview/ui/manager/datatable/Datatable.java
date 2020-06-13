package org.sourcelab.kafka.webview.ui.manager.datatable;

import org.sourcelab.kafka.webview.ui.model.View;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.persistence.criteria.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class Datatable<T> {
    private final JpaSpecificationExecutor<T> repository;
    private final Pageable pageable;
    private final Map<String, String> requestParams;
    private final String url;
    private final String label;
    private final List<DatatableColumn> columns;
    private final List<DatatableFilter> filters;
    private final DatatableSearch datatableSearch;

    // Generated properties
    private Page<T> page = null;

    public Datatable(final JpaSpecificationExecutor<T> repository, final Pageable pageable, final Map<String, String> requestParams, final String url, final String label, final List<DatatableColumn> columns, final List<DatatableFilter> filters, final DatatableSearch datatableSearch) {
        this.repository = Objects.requireNonNull(repository);
        this.pageable = Objects.requireNonNull(pageable);
        this.requestParams = Collections.unmodifiableMap(new HashMap<>(requestParams));
        this.url = url;
        this.label = label;
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
        if (hasSearch()) {
            params.put("search", getSearch().getCurrentSearchTerm());
        }

        // Add filters
        getFilters().forEach((filter) -> params.put(filter.getField(), getCurrentFilterValueFor(filter)));

        if (getPage().getSort().isSorted()) {
            final List<String> sortValues = new ArrayList<>();
            getPage().getSort().get().forEachOrdered((sort) -> {
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
        return getPage().getTotalElements();
    }

    public int getTotalPages() {
        return getPage().getTotalPages();
    }

    public int getNumber() {
        return getPage().getNumber();
    }

    public boolean isLastPage() {
        return getPage().isLast();
    }

    public boolean hasNextPage() {
        return getPage().hasNext();
    }

    public boolean hasPreviousPage() {
        return getPage().hasPrevious();
    }

    public boolean isFirstPage() {
        return getPage().isFirst();
    }

    public boolean isEmpty() {
        return getPage().isEmpty();
    }

    public List<T> getRecords() {
        return getPage().getContent();
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

    public Page<T> getPage() {
        if (this.page != null) {
            return this.page;
        }

        // Add search criteria
        String searchValue = null;
        if (hasSearch()) {
            searchValue = getSearch().getCurrentSearchTerm();
            if (searchValue != null && searchValue.isEmpty()) {
                searchValue = null;
            }
        }
        Specification<T> specification = Specification.where(
            searchValue == null ? null : (root, query, builder) -> builder.like(
                root.get(getSearch().getField()), "%" + getSearch().getCurrentSearchTerm() + "%"
            )
        );


        // Add filter criterias
        for (final DatatableFilter filter : getFilters()) {
            // Skip non-provided filters.
            if (getCurrentFilterValueFor(filter).isEmpty()) {
                continue;
            }

            specification = specification.and(
                (root, query, builder) -> {
                    final String[] fieldBits = filter.getField().split("\\.");
                    Path<Object> queryPath = root.get(fieldBits[0]);
                    for (int index = 1; index < fieldBits.length; index++) {
                        queryPath = queryPath.get(fieldBits[index]);
                    }
                    return builder.equal(queryPath, getCurrentFilterValueFor(filter));
                }
            );
        }

        // Execute
        page = repository.findAll(specification, pageable);
        return page;
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
        private JpaSpecificationExecutor<T> repository;
        private Map<String, String> requestParams = new HashMap<>();
        private Pageable pageable;
        private String url;
        private String label;
        private List<DatatableColumn> columns = new ArrayList<>();
        private List<DatatableFilter> filters = new ArrayList<>();
        private DatatableSearch datatableSearch;

        private Builder() {
        }

        public Builder<T> withRepository(final JpaSpecificationExecutor<T> repository) {
            this.repository = repository;
            return this;
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

        public Builder<T> withPageable(final Pageable pageable) {
            this.pageable = pageable;
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

        public Builder<T> withSearch(final String search, final String name) {
            return withSearch(search, name, null);
        }

        public Builder<T> withSearch(final String search, final String name, final String currentSearchTerm) {
            return withSearch(new DatatableSearch(search, name, currentSearchTerm));
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

            return new Datatable<>(repository, pageable, requestParams, url, label, columns, filters, datatableSearch);
        }
    }
}
