package com.darksci.kafka.webview.repository;

import com.darksci.kafka.webview.model.Cluster;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * For interacting w/ the Cluster database table.
 */
@Repository
public interface ClusterRepository extends CrudRepository<Cluster, Long> {
    /**
     * Find cluster by name.
     */
    Cluster findByName(final String name);

    /**
     * Retrieve all clusters ordered by name.
     */
    Iterable<Cluster> findAllByOrderByNameAsc();

//    @Query(value = "SELECT * FROM location l where l.user_id = :userId order by id desc limit :limit", nativeQuery = true)
//    List<Location> findLatest(@Param("userId") final long userId, @Param("limit") final int limit);
}
