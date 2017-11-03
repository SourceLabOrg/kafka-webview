package com.darksci.kafka.webview.ui.repository;

import com.darksci.kafka.webview.ui.model.ViewToFilterEnforced;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * For Accessing view_to_filter_enforced table.
 */
@Repository
public interface ViewToFilterEnforcedRepository extends CrudRepository<ViewToFilterEnforced, Long> {
    /**
     * Retrieve all by FilterId.
     */
    List<ViewToFilterEnforced> findByFilterId(final Long filterId);

    /**
     * Retrieve all by ViewId.
     */
    List<ViewToFilterEnforced> findByViewId(final Long viewId);
}
