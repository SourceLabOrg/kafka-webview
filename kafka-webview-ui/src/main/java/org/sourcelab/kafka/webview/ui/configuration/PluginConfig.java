package org.sourcelab.kafka.webview.ui.configuration;

import org.apache.kafka.common.serialization.Deserializer;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaAdminFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaConsumerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaOperationsFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.UploadManager;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Application Configuration for Plugin beans.
 */
@Component
public class PluginConfig {

    /**
     * Upload manager, for handling uploads of Plugins and Keystores.
     */
    @Bean
    public UploadManager getPluginUploadManager(final AppProperties appProperties) {
        return new UploadManager(appProperties.getUploadPath());
    }

    /**
     * PluginFactory for creating instances of Deserializers.
     */
    @Bean
    public PluginFactory<Deserializer> getDeserializerPluginFactory(final AppProperties appProperties) {
        final String jarDirectory = appProperties.getUploadPath() + "/deserializers";
        return new PluginFactory<>(jarDirectory, Deserializer.class);
    }

    /**
     * PluginFactory for creating instances of Record Filters.
     */
    @Bean
    public PluginFactory<RecordFilter> getRecordFilterPluginFactory(final AppProperties appProperties) {
        final String jarDirectory = appProperties.getUploadPath() + "/filters";
        return new PluginFactory<>(jarDirectory, RecordFilter.class);
    }

    /**
     * For handling secrets, symmetrical encryption.
     */
    @Bean
    public SecretManager getSecretManager(final AppProperties appProperties) {
        return new SecretManager(appProperties.getAppKey());
    }

    /**
     * For creating Kafka Consumers.
     */
    @Bean
    public WebKafkaConsumerFactory getWebKafkaConsumerFactory(final AppProperties appProperties) {
        return new WebKafkaConsumerFactory(
            getDeserializerPluginFactory(appProperties),
            getRecordFilterPluginFactory(appProperties),
            getSecretManager(appProperties),
            getKafkaConsumerFactory(appProperties)
        );
    }

    /**
     * For creating Kafka operational consumers.
     */
    @Bean
    public KafkaOperationsFactory getKafkaOperationsFactory(final AppProperties appProperties) {
        return new KafkaOperationsFactory(
            getSecretManager(appProperties),
            getKafkaAdminFactory(appProperties)
        );
    }

    /**
     * For creating instances of AdminClient.
     */
    private KafkaAdminFactory getKafkaAdminFactory(final AppProperties appProperties) {
        return new KafkaAdminFactory(appProperties.getUploadPath() + "/keyStores");
    }

    /**
     * For creating instances of KafkaConsumers.
     */
    private KafkaConsumerFactory getKafkaConsumerFactory(final AppProperties appProperties) {
        return new KafkaConsumerFactory(appProperties.getUploadPath() + "/keyStores");
    }
}
