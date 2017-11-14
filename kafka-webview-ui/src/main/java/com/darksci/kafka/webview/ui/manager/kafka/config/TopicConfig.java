/**
 * MIT License
 *
 * Copyright (c) 2017 Stephen Powis https://github.com/Crim/kafka-webview
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

package com.darksci.kafka.webview.ui.manager.kafka.config;

/**
 * Configuration defining which Cluster to connect to, which topic, and how to deserialize values.
 */
public class TopicConfig {
    private final ClusterConfig clusterConfig;
    private final DeserializerConfig deserializerConfig;
    private final String topicName;

    /**
     * Constructor.
     * @param clusterConfig Cluster configuration.
     * @param deserializerConfig Deserializer configuration.
     * @param topicName Topic definition.
     */
    public TopicConfig(final ClusterConfig clusterConfig, final DeserializerConfig deserializerConfig, final String topicName) {
        this.clusterConfig = clusterConfig;
        this.deserializerConfig = deserializerConfig;
        this.topicName = topicName;
    }

    public ClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    public DeserializerConfig getDeserializerConfig() {
        return deserializerConfig;
    }

    public String getTopicName() {
        return topicName;
    }

    @Override
    public String toString() {
        return "TopicConfig{"
            + "clusterConfig=" + clusterConfig
            + ", deserializerConfig=" + deserializerConfig
            + ", topicName='" + topicName + '\''
            + '}';
    }
}
