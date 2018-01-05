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

package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import java.util.Collections;
import java.util.List;

/**
 * Represents information about a set of records pulled from Kafka, and associated
 * metadata.
 */
public class KafkaResults {
    private final List<KafkaResult> results;
    private final List<PartitionOffset> consumerOffsets;
    private final List<PartitionOffset> headOffsets;
    private final List<PartitionOffset> tailOffsets;
    private final int numberOfRecords;

    /**
     * Constructor.
     */
    public KafkaResults(
        final List<KafkaResult> results,
        final List<PartitionOffset> consumerOffsets,
        final List<PartitionOffset> headOffsets,
        final List<PartitionOffset> tailOffsets) {
        this.results = Collections.unmodifiableList(results);
        this.numberOfRecords = results.size();
        this.consumerOffsets = Collections.unmodifiableList(consumerOffsets);
        this.headOffsets = Collections.unmodifiableList(headOffsets);
        this.tailOffsets = Collections.unmodifiableList(tailOffsets);
    }

    public List<KafkaResult> getResults() {
        return results;
    }


    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public List<PartitionOffset> getConsumerOffsets() {
        return consumerOffsets;
    }

    public List<PartitionOffset> getHeadOffsets() {
        return headOffsets;
    }

    public List<PartitionOffset> getTailOffsets() {
        return tailOffsets;
    }

    @Override
    public String toString() {
        return "KafkaResults{"
            + "results=" + results
            + ", consumerOffsets=" + consumerOffsets
            + ", headOffsets=" + headOffsets
            + ", tailOffsets=" + tailOffsets
            + ", numberOfRecords=" + numberOfRecords
            + '}';
    }
}
