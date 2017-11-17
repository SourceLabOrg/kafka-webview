package org.sourcelab.kafka.webview.ui.configuration;

import org.sourcelab.kafka.webview.ui.manager.user.UserManager;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.FloatDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.ShortDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Called on startup to ensure we have sane default data loaded.
 */
@Component
public final class DataLoaderConfig implements ApplicationRunner {

    private final MessageFormatRepository messageFormatRepository;
    private final UserRepository userRepository;

    /**
     * Constructor.
     */
    @Autowired
    private DataLoaderConfig(final MessageFormatRepository messageFormatRepository, final UserRepository userRepository) {
        this.messageFormatRepository = messageFormatRepository;
        this.userRepository = userRepository;
    }

    /**
     * Define the default MessageFormats and create if needed.
     */
    private void createData() {
        createDefaultUser();
        createDefaultMessageFormats();
    }

    /**
     * Creates default admin user if none exists.
     */
    private void createDefaultUser() {
        // If no users exist, create default admin user.
        if (userRepository.count() != 0) {
            return;
        }

        final UserManager userManager = new UserManager(userRepository);
        userManager.createNewUser(
            "admin@example.com",
            "Default Admin User",
            "admin",
            UserRole.ROLE_ADMIN
        );
    }

    /**
     * Creates default message formats.
     */
    private void createDefaultMessageFormats() {
        final Map<String, String> defaultFormats = new HashMap<>();
        defaultFormats.put("Short", ShortDeserializer.class.getName());
        defaultFormats.put("ByteArray", ByteArrayDeserializer.class.getName());
        defaultFormats.put("Bytes", BytesDeserializer.class.getName());
        defaultFormats.put("Double", DoubleDeserializer.class.getName());
        defaultFormats.put("Float", FloatDeserializer.class.getName());
        defaultFormats.put("Integer", IntegerDeserializer.class.getName());
        defaultFormats.put("Long", LongDeserializer.class.getName());
        defaultFormats.put("String", StringDeserializer.class.getName());

        // Create if needed.
        for (final Map.Entry<String, String> entry : defaultFormats.entrySet()) {
            MessageFormat messageFormat = messageFormatRepository.findByName(entry.getKey());
            if (messageFormat == null) {
                messageFormat = new MessageFormat();
            }
            messageFormat.setName(entry.getKey());
            messageFormat.setClasspath(entry.getValue());
            messageFormat.setJar("n/a");
            messageFormat.setDefaultFormat(true);
            messageFormatRepository.save(messageFormat);
        }
    }

    /**
     * Run on startup.
     */
    @Override
    public void run(final ApplicationArguments args) throws Exception {
        createData();
    }
}
