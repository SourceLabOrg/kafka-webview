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

import org.apache.kafka.clients.consumer.internals.AbstractPartitionAssignor;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This assignor will assign all partitions to all subscribed, thus ensuring all subscribers consume all records from all
 * partitions.
 */
public class DuplicatedAssignor extends AbstractPartitionAssignor {

    @Override
    public Map<String, List<TopicPartition>> assign(final Map<String, Integer> partitionsPerTopic, final Map<String, Subscription> subscriptions) {
        final ArrayList<TopicPartition> topicPartitions = new ArrayList<>();
        for (final Map.Entry<String, Integer> entry : partitionsPerTopic.entrySet()) {
            final String topic = entry.getKey();
            final Integer partitions = entry.getValue();
            if (partitions == null) {
                continue;
            }

            for (int partition = 0; partition < partitions; partition++) {
                topicPartitions.add(new TopicPartition(entry.getKey(), partition));
            }
        }

        final Map<String, List<TopicPartition>> assignment = new HashMap<>();
        for (final String memberId : subscriptions.keySet()) {
            assignment.put(memberId, topicPartitions);
        }
        return assignment;
    }

    @Override
    public String name() {
        return "duplicated";
    }
}
