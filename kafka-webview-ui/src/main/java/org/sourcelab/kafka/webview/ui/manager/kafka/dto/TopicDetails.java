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
 * Represents a Topic along with associated metadata.
 */
public class TopicDetails {
    private final String name;
    private final boolean isInternal;
    private final List<PartitionDetails> partitions;

    /**
     * Constructor.
     */
    public TopicDetails(final String name, final boolean isInternal, final List<PartitionDetails> partitions) {
        this.name = name;
        this.isInternal = isInternal;
        this.partitions = Collections.unmodifiableList(partitions);
    }

    public String getName() {
        return name;
    }

    public List<PartitionDetails> getPartitions() {
        return partitions;
    }

    public boolean isInternal() {
        return isInternal;
    }

    @Override
    public String toString() {
        return "TopicDetails{"
            + "+ name='" + name + '\''
            + ", + isInternal=" + isInternal
            + ", + partitions=" + partitions
            + '}';
    }
}
