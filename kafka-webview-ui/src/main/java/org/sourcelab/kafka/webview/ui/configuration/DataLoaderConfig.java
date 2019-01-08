/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.configuration;

import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.FloatDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.ShortDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.sourcelab.kafka.webview.ui.manager.user.UserManager;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.PartitioningStrategy;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.sourcelab.kafka.webview.ui.repository.PartitioningStrategyRepository;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
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
    private final PartitioningStrategyRepository partitioningStrategyRepository;

    /**
     * Constructor.
     */
    @Autowired
    private DataLoaderConfig(
        final MessageFormatRepository messageFormatRepository,
        final UserRepository userRepository,
        final PartitioningStrategyRepository partitioningStrategyRepository
    ) {
        this.messageFormatRepository = messageFormatRepository;
        this.userRepository = userRepository;
        this.partitioningStrategyRepository = partitioningStrategyRepository;
    }

    /**
     * Define the default MessageFormats and create if needed.
     */
    private void createData() {
        createDefaultUser();
        createDefaultMessageFormats();
        createDefaultPartitioningStrategies();
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
     * Creates default partitioning strategies.
     */
    private void createDefaultPartitioningStrategies() {
        final Map<String, String> defaultEntries = new HashMap<>();
        defaultEntries.put("Default Partitioner", DefaultPartitioner.class.getName());

        // Create if needed.
        for (final Map.Entry<String, String> entry : defaultEntries.entrySet()) {
            PartitioningStrategy partitioningStrategy = partitioningStrategyRepository.findByName(entry.getKey());
            if (partitioningStrategy == null) {
                partitioningStrategy = new PartitioningStrategy();
            }
            partitioningStrategy.setName(entry.getKey());
            partitioningStrategy.setClasspath(entry.getValue());
            partitioningStrategy.setJar("n/a");
            partitioningStrategy.setDefault(true);
            partitioningStrategyRepository.save(partitioningStrategy);
        }
    }

    /**
     * Run on startup.
     */
    @Override
    public void run(final ApplicationArguments args) {
        createData();
    }
}
