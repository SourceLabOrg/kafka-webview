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

package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TopicListTest {

    /**
     * By default assume no exception.
     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Validates we sort the topics.
     */
    @Test
    public void testListingIsSorted() {
        final String topic1 = "TopicA";
        final String topic2 = "AnotherTopic";
        final String topic3 = "123Topic";

        final List<TopicListing> topicListingList = new ArrayList<>();
        topicListingList.add(new TopicListing(topic1, false));
        topicListingList.add(new TopicListing(topic2, false));
        topicListingList.add(new TopicListing(topic3, false));

        // Create instance
        final TopicList topicList = new TopicList(topicListingList);
        final List<String> results = topicList.getTopicNames();

        // Validate its sorted
        assertEquals(topic3, results.get(0));
        assertEquals(topic2, results.get(1));
        assertEquals(topic1, results.get(2));
    }

    /**
     * Validates we get immutable out.
     */
    @Test
    public void testGetTopicNamesReturnsImmutable() {
        final List<TopicListing> topicListingList = new ArrayList<>();
        topicListingList.add(new TopicListing("A", false));
        topicListingList.add(new TopicListing("B", false));
        topicListingList.add(new TopicListing("C", false));

        // Create instance
        final TopicList topicList = new TopicList(topicListingList);
        final List<String> results = topicList.getTopicNames();

        // Should be immutable.
        expectedException.expect(UnsupportedOperationException.class);
        results.remove(1);
    }

    /**
     * Validates we get immutable out.
     */
    @Test
    public void testGetTopicsReturnsImmutable() {
        final List<TopicListing> topicListingList = new ArrayList<>();
        topicListingList.add(new TopicListing("D", false));
        topicListingList.add(new TopicListing("E", false));
        topicListingList.add(new TopicListing("F", false));

        // Create instance
        final TopicList topicList = new TopicList(topicListingList);
        final List<TopicListing> results = topicList.getTopics();

        // Should be immutable.
        expectedException.expect(UnsupportedOperationException.class);
        results.remove(1);
    }
}