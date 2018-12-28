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
package org.sourcelab.kafka.webview.ui.manager.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.producer.config.WebProducerConfig;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Intended to be an abstraction around KafkaProducer for use in publishing from a web interface.
 */
public class WebKafkaProducer {
    private final static Logger logger = LoggerFactory.getLogger(WebKafkaProducer.class);

    /**
     * The abstracted KafkaProducer instance for talking to kafka.
     */
    private final KafkaProducer kafkaProducer;

    /**
     * Configuration defining properties about how/what/where to producer.
     */
    private final WebProducerConfig producerConfig;

    /**
     * Constructor.
     * @param kafkaProducer The underlying/wrapped KafkaProducer instance.
     * @param producerConfig The client configuration.
     */
    public WebKafkaProducer(final KafkaProducer kafkaProducer, final WebProducerConfig producerConfig) {
        this.kafkaProducer = kafkaProducer;
        this.producerConfig = producerConfig;
    }

    /**
     * Producer a record to Kafka.
     * @param webRecord the record to publish.
     */
    public void produce(final WebProducerRecord webRecord) {
        // Convert Key to appropriate value.
        final Object key = producerConfig.getKeyTransformer().transform(
            webRecord.getKeyValues()
        );

        // Convert Value to appropriate value.
        final Object value = producerConfig.getValueTransformer().transform(
            webRecord.getValueValues()
        );

        // Create producer record.
        final ProducerRecord record = new ProducerRecord(
            producerConfig.getTopic(),
            key,
            value
        );

        // publish
        final Future future = kafkaProducer.send(record);

        // Wait for it to complete.
        try {
            future.get(
                producerConfig.getTimeoutMs(), TimeUnit.MILLISECONDS
            );
        } catch (final InterruptedException | ExecutionException | TimeoutException exception) {
            logger.error("Error publishing record: " + exception.getMessage(), exception);
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }
}
