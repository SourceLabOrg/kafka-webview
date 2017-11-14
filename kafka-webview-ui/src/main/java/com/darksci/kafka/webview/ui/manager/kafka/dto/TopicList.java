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

package com.darksci.kafka.webview.ui.manager.kafka.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a collection of Topics within a Cluster.
 */
public class TopicList {
    private final List<TopicListing> topics;

    /**
     * Constructor.
     */
    public TopicList(final List<TopicListing> topics) {
        this.topics = topics;
    }

    /**
     * @return a List of topics.
     */
    public List<TopicListing> getTopics() {
        return Collections.unmodifiableList(topics);
    }

    /**
     * @return a List of the topic names.
     */
    public List<String> getTopicNames() {
        final List<String> topicNames = new ArrayList<>();
        for (final TopicListing topicListing: getTopics()) {
            topicNames.add(topicListing.getName());
        }
        return Collections.unmodifiableList(topicNames);
    }

    @Override
    public String toString() {
        return "TopicList{"
            + "+ topics=" + topics
            + '}';
    }
}
