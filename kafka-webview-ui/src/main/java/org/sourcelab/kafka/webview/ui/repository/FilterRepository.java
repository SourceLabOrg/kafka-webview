package org.sourcelab.kafka.webview.ui.repository;

import org.sourcelab.kafka.webview.ui.model.Filter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * For accessing records on the filter table.
 */
@Repository
public interface FilterRepository extends CrudRepository<Filter, Long> {
    /**
     * Retrieve a Filter by the Name field.
     */
    Filter findByName(final String name);

    /**
     * Retrieve all Filters ordered by Name.
     */
    Iterable<Filter> findAllByOrderByNameAsc();
}
