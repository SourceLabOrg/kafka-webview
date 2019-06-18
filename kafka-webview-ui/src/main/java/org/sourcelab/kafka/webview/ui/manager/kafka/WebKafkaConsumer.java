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

package org.sourcelab.kafka.webview.ui.manager.kafka;

import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ConsumerState;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResults;

import java.util.Map;

/**
 * Interface for a KafkaConsumer used for consuming from Kafka from on-line web requests.
 */
public interface WebKafkaConsumer extends AutoCloseable {

    /**
     * Retrieves next batch of records per partition.
     * @return KafkaResults object containing any found records.
     */
    KafkaResults consumePerPartition();

    /**
     * Seek to the specified offsets.
     * @param partitionOffsetMap Map of PartitionId to Offset to seek to.
     * @return ConsumerState representing the consumer's positions.
     */
    ConsumerState seek(final Map<Integer, Long> partitionOffsetMap);

    /**
     * Seek consumer to specific timestamp
     * @param timestamp Unix timestamp in milliseconds to seek to.
     */
    ConsumerState seek(final long timestamp);

    /**
     * Closes out the consumer.
     */
    void close();

    /**
     * Seek to the previous 'page' of records.
     */
    void previous();

    /**
     * Seek to the next 'page' of records.
     */
    void next();

    /**
     * Seek to the HEAD of a topic.
     */
    ConsumerState toHead();

    /**
     * Seek to the TAIL of a topic.
     */
    ConsumerState toTail();
}
