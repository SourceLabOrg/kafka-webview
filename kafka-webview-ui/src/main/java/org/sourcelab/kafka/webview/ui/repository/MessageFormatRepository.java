package org.sourcelab.kafka.webview.ui.repository;

import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * For access records on the message_format table.
 */
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
