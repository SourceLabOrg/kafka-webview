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

package org.sourcelab.kafka.webview.ui.manager.kafka.producer.config;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.producer.transformer.ValueTransformer;

import java.util.concurrent.TimeUnit;

/**
 * Defines configuration values for producing into a kafka topic.
 */
public class WebProducerConfig {
    private final ClusterConfig clusterConfig;
    private final String topic;
    private final String producerClientId;

    // Serialization options
    private final ValueTransformer keyTransformer;
    private final ValueTransformer valueTransformer;

    // Partitioning Options
    private final Class<? extends Partitioner> partitionerClass;

    /**
     * Producer timeout, in millis seconds.  Defaults 15 secs.
     * TODO allow configuring.
     */
    private final long timeoutMs = TimeUnit.SECONDS.toMillis(15);

    /**
     * What level of acknowledgement is required during the publish.
     * TODO Allow configuring.
     */
    private final String ackRequirement = "1";

    /**
     * Constructor.
     * @param clusterConfig defines what cluster to connect to.
     * @param topic defines which topic to produce to.
     * @param producerClientId identifier for who is publishing.
     * @param keyTransformer how to serialize keys.
     * @param valueTransformer how to serialize values.
     * @param partitionerClass how to partition records published to kafka.
     */
    public WebProducerConfig(
        final ClusterConfig clusterConfig,
        final String topic,
        final String producerClientId,
        final ValueTransformer keyTransformer,
        final ValueTransformer valueTransformer,
        final Class<? extends Partitioner> partitionerClass
    ) {
        if (clusterConfig == null) {
            throw new IllegalArgumentException("Cluster Config may not be null");
        }
        if (topic == null) {
            throw new IllegalArgumentException("topic may not be null");
        }
        if (producerClientId == null) {
            throw new IllegalArgumentException("clientId may not be null");
        }
        if (keyTransformer == null) {
            throw new IllegalArgumentException("key transformer may not be null");
        }
        if (valueTransformer == null) {
            throw new IllegalArgumentException("value transformer may not be null");
        }
        if (partitionerClass == null) {
            throw new IllegalArgumentException("partitioner may not be null");
        }

        this.clusterConfig = clusterConfig;
        this.topic = topic;
        this.producerClientId = producerClientId;
        this.keyTransformer = keyTransformer;
        this.valueTransformer = valueTransformer;
        this.partitionerClass = partitionerClass;
    }

    public ClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    public String getTopic() {
        return topic;
    }

    public String getProducerClientId() {
        return producerClientId;
    }

    public ValueTransformer getKeyTransformer() {
        return keyTransformer;
    }

    public ValueTransformer getValueTransformer() {
        return valueTransformer;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public String getAckRequirement() {
        return ackRequirement;
    }

    public Class<? extends Partitioner> getPartitionerClass() {
        return partitionerClass;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builder instance for WebProducerConfig.
     */
    public static final class Builder {
        private ClusterConfig clusterConfig;
        private String topic;
        private String producerClientId;
        private ValueTransformer keyTransformer;
        private ValueTransformer valueTransformer;
        private Class<? extends Partitioner> partitionerClass = DefaultPartitioner.class;

        private Builder() {
        }

        public Builder withClusterConfig(ClusterConfig clusterConfig) {
            this.clusterConfig = clusterConfig;
            return this;
        }

        public Builder withTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder withProducerClientId(String producerClientId) {
            this.producerClientId = producerClientId;
            return this;
        }

        public Builder withKeyTransformer(ValueTransformer keyTransformer) {
            this.keyTransformer = keyTransformer;
            return this;
        }

        public Builder withValueTransformer(ValueTransformer valueTransformer) {
            this.valueTransformer = valueTransformer;
            return this;
        }

        public Builder withPartitionerClass(Class<? extends Partitioner> partitionerClass) {
            this.partitionerClass = partitionerClass;
            return this;
        }

        public WebProducerConfig build() {
            return new WebProducerConfig(clusterConfig, topic, producerClientId, keyTransformer, valueTransformer, partitionerClass);
        }
    }
}
