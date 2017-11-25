package org.sourcelab.kafka.webview.ui.tools;

import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helpful tools for Filters in tests.
 */
@Component
public class MessageFormatTestTools {
    private final MessageFormatRepository messageFormatRepository;

    @Autowired
    public MessageFormatTestTools(final MessageFormatRepository messageFormatRepository) {
        this.messageFormatRepository = messageFormatRepository;
    }

    /**
     * Utility for creating Filters.
     * @param name Name of the filter.
     * @return Persisted Filter.
     */
    public MessageFormat createMessageFormat(final String name) {
        final MessageFormat format = new MessageFormat();
        format.setName(name);
        format.setClasspath("com.example." + name);
        format.setJar(name + ".jar");
        format.setOptionParameters("{\"key\": \"value\"}");
        messageFormatRepository.save(format);

        return format;
    }
}
