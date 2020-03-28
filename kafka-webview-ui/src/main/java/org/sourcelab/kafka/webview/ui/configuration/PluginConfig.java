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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.file.FileManager;
import org.sourcelab.kafka.webview.ui.manager.file.FileStorageService;
import org.sourcelab.kafka.webview.ui.manager.file.FileType;
import org.sourcelab.kafka.webview.ui.manager.file.LocalDiskStorage;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaAdminFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaClientConfigUtil;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaConsumerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaOperationsFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.UploadManager;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslUtility;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application Configuration for Plugin beans.
 */
@Component
public class PluginConfig {
    private static final Logger logger = LoggerFactory.getLogger(PluginConfig.class);

    /**
     * Upload manager, for handling uploads of Plugins and Keystores.
     * @param fileManager for managing file persistence.
     * @return UploadManager for Plugins
     */
    @Bean
    public UploadManager getPluginUploadManager(final FileManager fileManager) {
        return new UploadManager(fileManager);
    }

    /**
     * PluginFactory for creating instances of Deserializers.
     * @param fileManager for managing file persistence.
     * @return PluginFactory for Deserializers.
     */
    @Bean("PluginFactoryForDeserializer")
    public PluginFactory<Deserializer> getDeserializerPluginFactory(final FileManager fileManager) {
        return new PluginFactory<>(FileType.DESERIALIZER, Deserializer.class, fileManager);
    }

    /**
     * PluginFactory for creating instances of Record Filters.
     * @param fileManager for managing file persistence.
     * @return PluginFactory for Record Filters.
     */
    @Bean("PluginFactoryForRecordFilter")
    public PluginFactory<RecordFilter> getRecordFilterPluginFactory(final FileManager fileManager) {
        return new PluginFactory<>(FileType.FILTER, RecordFilter.class, fileManager);
    }

    /**
     * For handling secrets, symmetrical encryption.
     * @param appProperties Definition of app properties.
     * @return SecretManager
     */
    @Bean
    public SecretManager getSecretManager(final AppProperties appProperties) {
        return new SecretManager(appProperties.getAppKey());
    }

    /**
     * For creating Kafka Consumers.
     * @param appProperties Definition of app properties.
     * @return Web Kafka Consumer Factory instance.
     */
    @Bean
    public WebKafkaConsumerFactory getWebKafkaConsumerFactory(
        final AppProperties appProperties,
        final KafkaClientConfigUtil configUtil,
        final PluginFactory<Deserializer> deserializerPluginFactory,
        final PluginFactory<RecordFilter> recordFilterPluginFactory,
        final SecretManager secretManager
    ) {
        final ExecutorService executorService;

        // If we have multi-threaded consumer option enabled
        if (appProperties.isEnableMultiThreadedConsumer()) {
            logger.info("Enabled multi-threaded webconsumer with {} threads.", appProperties.getMaxConcurrentWebConsumers());

            // Create fixed thread pool
            executorService = Executors.newFixedThreadPool(
                appProperties.getMaxConcurrentWebConsumers(),
                new ThreadFactoryBuilder()
                    .setNameFormat("kafka-web-consumer-pool-%d")
                   .build()
            );
        } else {
            // Null reference.
            executorService = null;
        }

        return new WebKafkaConsumerFactory(
            deserializerPluginFactory,
            recordFilterPluginFactory,
            secretManager,
            getKafkaConsumerFactory(configUtil),
            executorService
        );
    }

    /**
     * For creating Kafka operational consumers.
     * @param configUtil Utility for configuring kafka clients.
     * @return Web Kafka Operations Client Factory instance.
     */
    @Bean
    public KafkaOperationsFactory getKafkaOperationsFactory(
        final KafkaClientConfigUtil configUtil,
        final SecretManager secretManager
    ) {
        return new KafkaOperationsFactory(
            secretManager,
            getKafkaAdminFactory(configUtil)
        );
    }

    /**
     * Customize the jackson object map builder.
     * @return Jackson2ObjectMapperBuilderCustomizer instance.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer registerJacksonProtobufModule() {
        return jacksonObjectMapperBuilder -> {
            // Register custom protocol buffer serializer.
            jacksonObjectMapperBuilder.modulesToInstall(new ProtobufModule());
        };
    }

    @Autowired(required = true)
    public void configureJackson(ObjectMapper jackson2ObjectMapper) {
        jackson2ObjectMapper.registerModule(new ProtobufModule());
    }
    
    /**
     * For creating instances of AdminClient.
     */
    private KafkaAdminFactory getKafkaAdminFactory(final KafkaClientConfigUtil configUtil) {
        return new KafkaAdminFactory(
            configUtil
        );
    }

    /**
     * For creating instances of KafkaConsumers.
     */
    private KafkaConsumerFactory getKafkaConsumerFactory(final KafkaClientConfigUtil configUtil) {
        return new KafkaConsumerFactory(
            configUtil
        );
    }

    /**
     * Utility class for generating common kafka client configs.
     * @param appProperties Definition of app properties.
     * @return KafkaClientConfigUtil
     */
    @Bean
    public KafkaClientConfigUtil getKafkaClientConfigUtil(final AppProperties appProperties) {
        return new KafkaClientConfigUtil(
            appProperties.getUploadPath() + "/keyStores",
            appProperties.getConsumerIdPrefix()
        );
    }

    /**
     * Utility for managing Sasl properties persisted on cluster table.
     * @param secretManager For handling encryption/decryption of secrets.
     * @return SaslUtility instance.
     */
    @Bean
    public SaslUtility getSaslUtility(final SecretManager secretManager) {
        return new SaslUtility(secretManager);
    }

    @Bean
    public FileStorageService fileStorageService(final AppProperties appProperties) {
        return new LocalDiskStorage(appProperties.getUploadPath());
    }

    /**
     * Utility for managing file storage operations.
     * @param fileStorageService Where to back filestorage.
     * @param appProperties Definition of app properties.
     * @return FileManager instance.
     */
    @Bean
    public FileManager fileManager(final FileStorageService fileStorageService, final AppProperties appProperties) {
        final LocalDiskStorage localCacheStorage = new LocalDiskStorage(appProperties.getCachePath());
        return new FileManager(fileStorageService, localCacheStorage);
    }
}
