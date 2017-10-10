package com.darksci.kafkaview.repository;

import com.darksci.kafkaview.model.View;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewRepository extends CrudRepository<View, Long> {
    View findByName(final String name);
    Iterable<View> findAllByOrderByNameAsc();
}
