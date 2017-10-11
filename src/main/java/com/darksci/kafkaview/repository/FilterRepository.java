package com.darksci.kafkaview.repository;

import com.darksci.kafkaview.model.Filter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRepository extends CrudRepository<Filter, Long> {
    Filter findByName(final String name);
    Iterable<Filter> findAllByOrderByNameAsc();
}
