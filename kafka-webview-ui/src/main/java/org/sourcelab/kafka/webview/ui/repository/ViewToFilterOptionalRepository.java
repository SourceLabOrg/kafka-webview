package org.sourcelab.kafka.webview.ui.repository;

import org.sourcelab.kafka.webview.ui.model.ViewToFilterOptional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * For Accessing view_to_filter_optional table.
 */
@Repository
public interface ViewToFilterOptionalRepository extends CrudRepository<ViewToFilterOptional, Long> {
    /**
     * Retrieve all by FilterId.
     */
    List<ViewToFilterOptional> findByFilterId(final Long filterId);

    /**
     * Retrieve all by ViewId.
     */
    List<ViewToFilterOptional> findByViewId(final Long viewId);
}
