package com.darksci.kafka.webview.ui.repository;

import com.darksci.kafka.webview.ui.model.MessageFormat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageFormatRepository extends CrudRepository<MessageFormat, Long> {
    /**
     * Retrieve by name.
     * @param name Name to search for.
     * @return MessageFormat found, or null.
     */
    MessageFormat findByName(final String name);

    /**
     * Find all message formats ordered by name.
     */
    Iterable<MessageFormat> findAllByOrderByNameAsc();

    /**
     * Find all message formats by type, ordered by name.
     */
    Iterable<MessageFormat> findByIsDefaultFormatOrderByNameAsc(final boolean isDefaultFormat);
}
