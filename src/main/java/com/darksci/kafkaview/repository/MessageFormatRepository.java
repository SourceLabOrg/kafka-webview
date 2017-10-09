package com.darksci.kafkaview.repository;

import com.darksci.kafkaview.model.MessageFormat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageFormatRepository extends CrudRepository<MessageFormat, Long> {
    MessageFormat findByName(final String name);
}
