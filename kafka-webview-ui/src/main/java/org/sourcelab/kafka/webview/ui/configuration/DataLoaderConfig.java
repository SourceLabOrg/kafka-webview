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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

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
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sourcelab.kafka.webview.ui.plugin.deserializer.DeserializerDiscoveryService;
import org.sourcelab.kafka.webview.ui.plugin.deserializer.DeserializerInformation;
import org.sourcelab.kafka.webview.ui.model.MessageFormatType;

/**
 * Called on startup to ensure we have sane default data loaded.
 */
@Component
public final class DataLoaderConfig implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoaderConfig.class);

    private final AppProperties appProperties;
    private final MessageFormatRepository messageFormatRepository;
    private final UserRepository userRepository;
    private final ObjectMapper mapper;

    /**
     * Constructor.
     */
    @Autowired
    private DataLoaderConfig(
        final AppProperties appProperties,
        final MessageFormatRepository messageFormatRepository,
        final UserRepository userRepository,
        final ObjectMapper mapper) {
        this.appProperties = appProperties;
        this.messageFormatRepository = messageFormatRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    /**
     * Define the default MessageFormats and create if needed.
     */
    private void createData() {
        createDefaultUser();
        createDefaultMessageFormats();
        discoverDeserializers();
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
            messageFormat.setMessageFormatType(MessageFormatType.DEFAULT);
            messageFormatRepository.save(messageFormat);
        }
    }

    private void discoverDeserializers() {
        final File deserializerPath = new File(appProperties.getDeserializerPath());
        
        // Check preconditions
        if (!deserializerPath.exists()) {
            logger.warn("Directory {} doesn't exists.", deserializerPath);
            return;
        }
        if (!deserializerPath.isDirectory()) {
            logger.error("{} is not a directory.", deserializerPath);
            return;
        }

        File[] jars = deserializerPath.listFiles((File dir, String name) -> name.endsWith(".jar"));
        logger.info("Analyse {} for deserializer", (Object) jars);
        for (File jar : jars) {
            try {
                URL jarUrl = jar.toURI().toURL();
                ClassLoader cl = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());
                ServiceLoader<DeserializerDiscoveryService> services = ServiceLoader.load(DeserializerDiscoveryService.class, cl);
                loadDeserializers(jar, services);
            } catch (MalformedURLException ex) {
                logger.error("Failed to load {}, error: {}", jar, ex.getMessage());
            }
        }
    }
    
    private void loadDeserializers(File jar, ServiceLoader<DeserializerDiscoveryService> services) {
        for (DeserializerDiscoveryService service : services) {
            List<DeserializerInformation> deserializersInformation = service.getDeserializersInformation();
            for (DeserializerInformation deserializerInformation : deserializersInformation) {
                try {
                    MessageFormat messageFormat = messageFormatRepository.findByName(deserializerInformation.getName());
                    if (messageFormat == null) {
                        messageFormat = new MessageFormat();
                    } else if (MessageFormatType.AUTOCONF != messageFormat.getMessageFormatType()) {
                        logger.error("Try to register the formatter {} but this name is already register as a {}.",
                            messageFormat.getName(), messageFormat.getMessageFormatType());
                        continue;
                    }
                    messageFormat.setName(deserializerInformation.getName());
                    messageFormat.setClasspath(deserializerInformation.getClasspath());
                    messageFormat.setJar(jar.getName());
                    messageFormat.setMessageFormatType(MessageFormatType.AUTOCONF);
                    messageFormat.setOptionParameters(
                        mapper.writeValueAsString(deserializerInformation.getDefaultConfig()));
                    messageFormatRepository.save(messageFormat);
                } catch (JsonProcessingException ex) {
                    logger.error("Failed to load {}, because the default config are invalid ({})",
                        deserializerInformation.getName(), ex.getMessage());
                }
            }
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
