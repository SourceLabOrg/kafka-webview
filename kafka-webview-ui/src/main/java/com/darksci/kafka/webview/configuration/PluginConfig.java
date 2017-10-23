package com.darksci.kafka.webview.configuration;

import com.darksci.kafka.webview.manager.encryption.SecretManager;
import com.darksci.kafka.webview.manager.kafka.KafkaAdminFactory;
import com.darksci.kafka.webview.manager.kafka.KafkaConsumerFactory;
import com.darksci.kafka.webview.manager.kafka.KafkaOperationsFactory;
import com.darksci.kafka.webview.manager.kafka.WebKafkaConsumerFactory;
import com.darksci.kafka.webview.manager.plugin.PluginFactory;
import com.darksci.kafka.webview.manager.plugin.UploadManager;
import com.darksci.kafka.webview.plugin.filter.RecordFilter;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class PluginConfig {

    @Bean
    public UploadManager getPluginUploadManager(final AppProperties appProperties) {
        return new UploadManager(appProperties.getUploadPath());
    }

    @Bean
    public PluginFactory<Deserializer> getDeserializerPluginFactory(final AppProperties appProperties) {
        final String jarDirectory = appProperties.getUploadPath() + "/deserializers";
        return new PluginFactory<>(jarDirectory, Deserializer.class);
    }

    @Bean
    public PluginFactory<RecordFilter> getRecordFilterPluginFactory(final AppProperties appProperties) {
        final String jarDirectory = appProperties.getUploadPath() + "/filters";
        return new PluginFactory<>(jarDirectory, RecordFilter.class);
    }

    @Bean
    public KafkaAdminFactory getKafkaAdminFactory(final AppProperties appProperties) {
        return new KafkaAdminFactory(appProperties.getUploadPath() + "/keyStores");
    }

    @Bean
    public KafkaConsumerFactory getKafkaConsumerFactory(final AppProperties appProperties) {
        return new KafkaConsumerFactory(appProperties.getUploadPath() + "/keyStores");
    }

    @Bean
    public SecretManager getSecretManager(final AppProperties appProperties) {
        return new SecretManager(appProperties.getAppKey());
    }

    @Bean
    public WebKafkaConsumerFactory getWebKafkaConsumerFactory(final AppProperties appProperties) {
        return new WebKafkaConsumerFactory(
            getDeserializerPluginFactory(appProperties),
            getRecordFilterPluginFactory(appProperties),
            getSecretManager(appProperties),
            getKafkaConsumerFactory(appProperties)
        );
    }

    @Bean
    public KafkaOperationsFactory getKafkaOperationsFactory(final AppProperties appProperties) {
        return new KafkaOperationsFactory(
            getSecretManager(appProperties),
            getKafkaAdminFactory(appProperties)
        );
    }
}
