package com.darksci.kafka.webview.repository;

import com.darksci.kafka.webview.model.Filter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRepository extends CrudRepository<Filter, Long> {
    Filter findByName(final String name);
    Iterable<Filter> findAllByOrderByNameAsc();
}
