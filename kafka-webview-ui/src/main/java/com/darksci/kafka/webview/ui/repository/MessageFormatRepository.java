package com.darksci.kafka.webview.ui.repository;

import com.darksci.kafka.webview.ui.model.MessageFormat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageFormatRepository extends CrudRepository<MessageFormat, Long> {
    MessageFormat findByName(final String name);
    Iterable<MessageFormat> findAllByOrderByNameAsc();
    Iterable<MessageFormat> findByIsDefaultFormatOrderByNameAsc(final boolean isDefaultFormat);
}
