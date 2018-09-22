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

/**
 * DTO class for defining a new Kafka topic.
 */
public class CreateTopic {
    private final String name;
    private final int numberOfPartitions;
    private final short replicaFactor;

    public CreateTopic(final String name, final int numberOfPartitions, final short replicaFactor) {
        this.name = name;
        this.numberOfPartitions = numberOfPartitions;
        this.replicaFactor = replicaFactor;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    public short getReplicaFactor() {
        return replicaFactor;
    }

    @Override
    public String toString() {
        return "CreateTopic{"
            + "name='" + name + '\''
            + ", numberOfPartitions=" + numberOfPartitions
            + ", replicaFactor=" + replicaFactor
            + '}';
    }
}
