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

package org.sourcelab.kafka.webview.ui.controller.api;

/**
 * Represents a request to create a new topic.
 */
public class CreateTopicRequest {
    private String name;
    private Integer partitions;
    private Short replicas;

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getPartitions() {
        return partitions;
    }

    public void setPartitions(final Integer partitions) {
        this.partitions = partitions;
    }

    public Short getReplicas() {
        return replicas;
    }

    public void setReplicas(final Short replicas) {
        this.replicas = replicas;
    }

    @Override
    public String toString() {
        return "CreateTopicRequest{"
            + "name='" + name + '\''
            + ", partitions=" + partitions
            + ", replicas=" + replicas
            + '}';
    }
}
