package com.darksci.kafkaview.repository;

import com.darksci.kafkaview.model.Location;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends CrudRepository<Location, Long> {
    Location findByIdAndUserId(final long id, final long userId);

    @Query(value = "SELECT * FROM location l where l.user_id = :userId order by id desc limit :limit", nativeQuery = true)
    List<Location> findLatest(@Param("userId") final long userId, @Param("limit") final int limit);
}

