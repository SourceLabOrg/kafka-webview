/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        final List<TopicListing> sortedList = new ArrayList<>();
        sortedList.addAll(topics);
        Collections.sort(sortedList, Comparator.comparing(TopicListing::getName));

        this.topics = Collections.unmodifiableList(sortedList);
    }

    /**
     * @return a List of topics.
     */
    public List<TopicListing> getTopics() {
        return topics;
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

    /**
     * Given a search value for topic names, return all entries that match search.
     * @param search search string.
     * @return filtered TopicList.
     */
    public TopicList filterByTopicName(final String search) {
        // Null means match nothing.
        if (search == null) {
            return new TopicList(Collections.emptyList());
        }
        final String normalizedSearch = search.toLowerCase();

        final List<TopicListing> topicListings = new ArrayList<>();
        for (final TopicListing topicListing : getTopics()) {
            if (topicListing.getName().toLowerCase().contains(normalizedSearch)) {
                topicListings.add(topicListing);
            }
        }

        return new TopicList(topicListings);
    }

    @Override
    public String toString() {
        return "TopicList{"
            + "+ topics=" + topics
            + '}';
    }
}
