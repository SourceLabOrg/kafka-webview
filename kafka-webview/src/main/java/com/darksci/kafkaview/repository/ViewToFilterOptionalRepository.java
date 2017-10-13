package com.darksci.kafkaview.repository;

import com.darksci.kafkaview.model.ViewToFilterOptional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewToFilterOptionalRepository extends CrudRepository<ViewToFilterOptional, Long> {
    List<ViewToFilterOptional> findByFilterId(final Long filterId);
    List<ViewToFilterOptional> findByViewId(final Long viewId);
}
