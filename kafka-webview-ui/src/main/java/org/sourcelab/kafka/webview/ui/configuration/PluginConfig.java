/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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
