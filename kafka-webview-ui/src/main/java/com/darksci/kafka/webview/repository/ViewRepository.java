package com.darksci.kafka.webview.repository;

import com.darksci.kafka.webview.model.View;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * For interacting w/ the View database table.
 */
@Repository
public interface ViewRepository extends CrudRepository<View, Long> {
    /**
     * Retrieve a view by its name.
     */
    View findByName(final String name);

    /**
     * Retrieve all views ordered by name.
     */
    Iterable<View> findAllByOrderByNameAsc();

    /**
     * Retrieve all views ordered by name for a given clusterId.
     */
    Iterable<View> findAllByClusterIdOrderByNameAsc(final long clusterId);

    /**
     * Count how many views exist for a given clusterId.
     */
    Long countByClusterId(final long clusterId);
}
