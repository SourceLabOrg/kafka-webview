package com.darksci.kafka.webview.ui.repository;

import com.darksci.kafka.webview.ui.model.Filter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRepository extends CrudRepository<Filter, Long> {
    Filter findByName(final String name);
    Iterable<Filter> findAllByOrderByNameAsc();
}
