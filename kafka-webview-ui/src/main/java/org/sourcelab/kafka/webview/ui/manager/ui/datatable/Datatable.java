/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.persistence.criteria.Path;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.sourcelab.kafka.webview.ui.manager.ui.datatable.ConstraintOperator.EQUALS;

/**
 * Aims to be a re-usable datatable UI component backed by a JPA Repository.
 * @param <T> Type of object being rendered in the datatable.
 */
public class Datatable<T> {
    private final JpaSpecificationExecutor<T> repository;
    private final Pageable pageable;
    private final Map<String, String> requestParams;
    private final String url;
    private final String label;

    /**
     * Defines query constraints.
     */
    private final List<DatatableConstraint> constraints;

    /**
     * Columns to display in the table.
     */
    private final List<DatatableColumn> columns;

    /**
     * Zero or more filters to render on the table.
     */
    private final List<DatatableFilter> filters;

    /**
     * Zero or more links at the top of the table.
     */
    private final List<DatatableLink> links;

    /**
     * Search Field.
     */
    private final DatatableSearch datatableSearch;

    /**
     * Defines which template to render for when no records are found.
     */
    private final String noRecordsFoundTemplatePath;

    // Generated properties
    private Page<T> page = null;

    /**
     * Constructor.  See Builder instance.
     * @param repository Underlying JPA repository instance.
     * @param pageable Pageable instance.
     * @param requestParams All request parameters available.
     * @param url URL the datatable lives at.
     * @param label Display label for the datatable.
     * @param columns One or more columns in the datatable.
     * @param filters Zero or more filters for the table.
     * @param datatableSearch Zero or one search.
     * @param noRecordsFoundTemplatePath What template to load if no records are found.
     */
    public Datatable(
        final JpaSpecificationExecutor<T> repository,
        final Pageable pageable,
        final List<DatatableConstraint> constraints,
        final Map<String, String> requestParams,
        final String url,
        final String label,
        final List<DatatableColumn> columns,
        final List<DatatableFilter> filters,
        final List<DatatableLink> links,
        final DatatableSearch datatableSearch,
        final String noRecordsFoundTemplatePath
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.pageable = Objects.requireNonNull(pageable);
        this.constraints = Objects.requireNonNull(constraints);
        this.requestParams = Collections.unmodifiableMap(new HashMap<>(requestParams));
        this.url = url;
        this.label = label;
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
        this.filters = Collections.unmodifiableList(new ArrayList<>(filters));
        this.datatableSearch = datatableSearch;
        this.links = Collections.unmodifiableList(new ArrayList<>(links));
        this.noRecordsFoundTemplatePath = Objects.requireNonNull(noRecordsFoundTemplatePath);
    }

    /**
     * The base URL the datatable lives at.
     * @return base URL the datatable lives at.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Map of all table parameters as currently defined.
     * @param overrides Allows for specifying key and value overrides for parameters.
     * @return Map of all table parameters as currently defined.
     */
    public Map<String, String> getUrlParams(final String ... overrides) {
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

        params.put("sort", "");
        if (getPage().getSort().isSorted()) {
            final List<String> sortValues = new ArrayList<>();
            getPage().getSort().get().forEachOrdered((sort) -> {
                sortValues.add(sort.getProperty() + "," + sort.getDirection());
            });
            params.put("sort", String.join(",", sortValues));
        }
        params.put("page", Integer.toString(getNumber()));

        // Override params
        for (final Map.Entry<String, String> overrideEntry : overrideParams.entrySet()) {
            // Allow for injecting/overriding specific params.
            String key = overrideEntry.getKey();
            String value = overrideEntry.getValue();

            // override values
            params.put(key, value);
        }

        // Remove any empty params.
        final Set<String> removeKeys = new HashSet<>();
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                removeKeys.add(entry.getKey());
            }
        }
        removeKeys.forEach(params::remove);

        return params;
    }

    /**
     * Generates the URL for the current page in the datatable with all applicable parameters appended.
     * @param overrides Key/values to override parameters for.
     * @return String representing the URL.
     */
    public String getUrlWithParams(final String ... overrides) {
        final Map<String, String> params = getUrlParams(overrides);

        final List<String> paramStr = new ArrayList<>();
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            // Allow for injecting/overriding specific params.
            String key = entry.getKey();
            String value = entry.getValue();

            // Skip if empty
            if (value == null || value.isEmpty()) {
                continue;
            }

            // Add and URL Encode params.
            try {
                paramStr.add(
                    URLEncoder.encode(key, StandardCharsets.UTF_8.name())
                    + "="
                    + URLEncoder.encode(value, StandardCharsets.UTF_8.name())
                );
            } catch (final UnsupportedEncodingException exception) {
                throw new RuntimeException(exception.getMessage(), exception);
            }
        }
        return getUrl() + "?" + String.join("&", paramStr);
    }

    /**
     * Gets the current filter value for a given filter.
     * @param filter The filter to get the current value for.
     * @return The current value of the supplied filter, or empty string if none currently set.
     */
    public String getCurrentFilterValueFor(final DatatableFilter filter) {
        final String param = filter.getField();
        if (requestParams.containsKey(param)) {
            return requestParams.get(param);
        }
        return "";
    }

    /**
     * Top level label for the datatable.
     * @return Top level label for the datatable.
     */
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

    public List<DatatableLink> getLinks() {
        return links;
    }

    public boolean hasFilters() {
        return filters != null && !filters.isEmpty();
    }

    public boolean hasSearch() {
        return datatableSearch != null;
    }

    public boolean hasLinks() {
        return links != null && !links.isEmpty();
    }

    public DatatableSearch getSearch() {
        return datatableSearch;
    }

    public String getNoRecordsFoundTemplatePath() {
        return noRecordsFoundTemplatePath;
    }

    private Page<T> getPage() {
        // Return calculated value if already set.
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
        Specification<T> specification;
        if (searchValue == null) {
            specification = Specification.where(null);
        } else {
            specification = Specification.where((root, query, builder) ->
                builder.like(
                    builder.lower(root.get(getSearch().getField())), "%" + getSearch().getCurrentSearchTerm().toLowerCase() + "%"
                )
            );
        }

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

        // Add enforced constraints
        for (final DatatableConstraint constraint : constraints) {
            specification = specification.and(
                (root, query, builder) -> {
                    final Path<Object> queryPath = root.get(constraint.getField());

                    switch (constraint.getOperator()) {
                        case EQUALS:
                            return builder.equal(queryPath, constraint.getValue());
                        default:
                            throw new RuntimeException("Unhandle operator");
                    }
                }
            );
        };


        // Execute
        page = repository.findAll(specification, pageable);
        return page;
    }

    /**
     * Get the current sort order for the given column.
     * @param column The column to get the current sort order for.
     * @return "desc" or "asc"
     */
    public String getCurrentSortOrderFor(final DatatableColumn column) {
        Objects.requireNonNull(column);
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

    /**
     * Determine if the datatable is currently sorted by the given column.
     * @param column The column to determine if it's currently sorted by.
     * @return true if yes, false if not.
     */
    public boolean isSortedBy(final DatatableColumn column) {
        Objects.requireNonNull(column);
        if (!column.isSortable()) {
            return false;
        }
        final Sort.Order order = page.getSort().getOrderFor(column.getField());
        if (order == null) {
            return false;
        }
        return true;
    }

    /**
     * For the current column, get the inverse of the current sort order.
     * IE, if the column is currently sorted ASC, this will return DESC.
     * @param column The column to get the sort order for.
     * @return inverted sort order.
     */
    public String getInverseSortOrderFor(final DatatableColumn column) {
        Objects.requireNonNull(column);

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

    /**
     * Create a new builder instance.
     * @param type The type of object being rendered in the datatable.
     * @param <T> The type of object being rendered in the datatable.
     * @return new Builder instance.
     */
    public static <T> Builder<T> newBuilder(Class<T> type) {
        return new Builder<T>();
    }

    /**
     * Builder instance for Datatable.
     * @param <T> Type of object being rendered in the datatable.
     */
    public static final class Builder<T> {
        private JpaSpecificationExecutor<T> repository;
        private Map<String, String> requestParams = new HashMap<>();
        private Pageable pageable;
        private String url;
        private String label;
        private List<DatatableColumn> columns = new ArrayList<>();
        private List<DatatableFilter> filters = new ArrayList<>();
        private DatatableSearch datatableSearch;
        private List<DatatableConstraint> constraints = new ArrayList<>();
        private List<DatatableLink> links = new ArrayList<>();

        // Default no records found template.
        private String noRecordsFoundTemplatePath = "fragments/datatable/DefaultNoRecordsFound";

        private Builder() {
        }

        public Builder<T> withRepository(final JpaSpecificationExecutor<T> repository) {
            this.repository = repository;
            return this;
        }

        /**
         * Add the RequestParameters assocated with the current request.
         * Used to pull current values for various filters/searches/etc..
         *
         * @param requestParams Hashmap of Request parameters and values.
         * @return Builder instance.
         */
        public Builder<T> withRequestParams(final Map<String, String> requestParams) {
            Objects.requireNonNull(requestParams);

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

        public Builder<T> withSearch(final String name) {
            return withSearch("Search...", name, null);
        }

        public Builder<T> withSearch(final String search, final String name) {
            return withSearch(search, name, null);
        }

        public Builder<T> withSearch(final String search, final String name, final String currentSearchTerm) {
            return withSearch(new DatatableSearch(search, name, currentSearchTerm));
        }

        public Builder<T> withCreateLink(final String url) {
            return withLink(new DatatableLink(url, "Create new", "icon-settings"));
        }

        public Builder<T> withLink(final String url, final String label, final String icon) {
            return withLink(new DatatableLink(url, label, icon));
        }

        public Builder<T> withLink(final DatatableLink link) {
            links.add(link);
            return this;
        }

        public Builder<T> withNoRecordsFoundTemplate(final String templatePath) {
            this.noRecordsFoundTemplatePath = templatePath;
            return this;
        }

        public Builder<T> withConstraint(final String field, final Object value, final ConstraintOperator operator) {
            return this.withConstraint(new DatatableConstraint(field, value, operator));
        }

        public Builder<T> withConstraint(final DatatableConstraint constraint) {
            this.constraints.add(constraint);
            return this;
        }

        /**
         * Create new Datatable instance from builder.
         * @return New datatable instance.
         */
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

            return new Datatable<>(
                repository,
                pageable,
                constraints,
                requestParams,
                url,
                label,
                columns,
                filters,
                links,
                datatableSearch,
                noRecordsFoundTemplatePath
            );
        }
    }
}
