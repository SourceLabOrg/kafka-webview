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
 * Represent details about a specific partition.
 */
public class PartitionDetails {
    private final String topic;
    private final int partition;
    private final NodeDetails leader;
    private final List<NodeDetails> replicas;
    private final List<NodeDetails> isr;
    private final boolean isUnderReplicated;

    /**
     * Constructor.
     */
    public PartitionDetails(
        final String topic,
        final int partition,
        final NodeDetails leader,
        final List<NodeDetails> replicas,
        final List<NodeDetails> isr
    ) {
        this.topic = topic;
        this.partition = partition;
        this.leader = leader;
        this.replicas = Collections.unmodifiableList(replicas);
        this.isr = Collections.unmodifiableList(isr);

        // Lazy many way to determine this is to count the number of replicas
        // and compare it to the count of ISR, if not equal, then under replicated.
        this.isUnderReplicated = getReplicas().size() != getIsr().size();
    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    public NodeDetails getLeader() {
        return leader;
    }

    public List<NodeDetails> getReplicas() {
        return replicas;
    }

    public List<NodeDetails> getIsr() {
        return isr;
    }

    public boolean isUnderReplicated() {
        return isUnderReplicated;
    }

    @Override
    public String toString() {
        return "PartitionDetails{"
            + "topic='" + topic + '\''
            + ", partition=" + partition
            + ", leader=" + leader
            + ", replicas=" + replicas
            + ", isr=" + isr
            + ", isUnderReplicated=" + isUnderReplicated
            + '}';
    }
}
