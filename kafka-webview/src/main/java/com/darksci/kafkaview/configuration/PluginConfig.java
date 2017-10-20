package com.darksci.kafkaview.configuration;

import com.darksci.kafkaview.manager.encryption.SecretManager;
import com.darksci.kafkaview.manager.kafka.KafkaAdminFactory;
import com.darksci.kafkaview.manager.kafka.KafkaConsumerFactory;
import com.darksci.kafkaview.manager.plugin.PluginFactory;
import com.darksci.kafkaview.manager.plugin.PluginSecurityPolicy;
import com.darksci.kafkaview.manager.plugin.UploadManager;
import com.darksci.kafkaview.plugin.filter.RecordFilter;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.security.Policy;

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
}
