package com.darksci.kafkaview.repository;

import com.darksci.kafkaview.model.Cluster;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ClusterRepository extends CrudRepository<Cluster, Long> {
    Cluster findByName(final String name);
    Iterable<Cluster> findAllByOrderByNameAsc();

//    @Query(value = "SELECT * FROM location l where l.user_id = :userId order by id desc limit :limit", nativeQuery = true)
//    List<Location> findLatest(@Param("userId") final long userId, @Param("limit") final int limit);
}
