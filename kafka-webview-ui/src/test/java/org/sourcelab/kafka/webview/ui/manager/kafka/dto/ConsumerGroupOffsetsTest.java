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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Simple test cases over how ConsumerGroupOffsets gets serialized.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ConsumerGroupOffsetsTest {

    /**
     * Reference to springboot's configured object mapper.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Validates the builder constructs an appropriate object.
     */
    @Test
    public void testBuilder() {
        final ConsumerGroupOffsets.Builder builder = ConsumerGroupOffsets.newBuilder()
            .withConsumerId("MyConsumerId")
            .withOffset("topic-a", 0, 0)
            .withOffset("topic-a", 1, 1)
            .withOffset("topic-b", 0, 2)
            .withOffset("topic-b", 1, 3);

        final ConsumerGroupOffsets offsets = builder.build();

        // Validate constructed properly.
        assertEquals("Should have proper identifier", "MyConsumerId", offsets.getConsumerId());
        assertTrue("Should have topic a", offsets.getTopicNames().contains("topic-a"));
        assertTrue("Should have topic b", offsets.getTopicNames().contains("topic-b"));
        assertEquals("Should have only 2 topics", 2, offsets.getTopicNames().size());

        // Validate topic A
        final ConsumerGroupTopicOffsets topicAOffsets = offsets.getOffsetsForTopic("topic-a");
        assertEquals("Has correct topic name", "topic-a", topicAOffsets.getTopic());
        assertTrue("has partition 0", topicAOffsets.getPartitions().contains(0));
        assertTrue("has partition 1", topicAOffsets.getPartitions().contains(1));
        assertEquals("Only has 2 partitions", 2, topicAOffsets.getPartitions().size());
        assertEquals("Only has 2 partitions", 2, topicAOffsets.getOffsets().size());
        assertEquals("Has correct offset for partition 0", 0, topicAOffsets.getOffsetForPartition(0));
        assertEquals("Has correct offset for partition 1", 1, topicAOffsets.getOffsetForPartition(1));

        // Validate topic B
        final ConsumerGroupTopicOffsets topicBOffsets = offsets.getOffsetsForTopic("topic-b");
        assertEquals("Has correct topic name", "topic-b", topicBOffsets.getTopic());
        assertTrue("has partition 0", topicBOffsets.getPartitions().contains(0));
        assertTrue("has partition 1", topicBOffsets.getPartitions().contains(1));
        assertEquals("Only has 2 partitions", 2, topicBOffsets.getPartitions().size());
        assertEquals("Only has 2 partitions", 2, topicBOffsets.getOffsets().size());
        assertEquals("Has correct offset for partition 0", 2, topicBOffsets.getOffsetForPartition(0));
        assertEquals("Has correct offset for partition 1", 3, topicBOffsets.getOffsetForPartition(1));
    }

    /**
     * TODO borked 3/25/2020
     * Validates the object serializes to json correctly.
     */
//    @Test
//    public void testSerialization() throws JsonProcessingException {
//        // Define our expected output.
//        final String expectedResult = "{\"consumerId\":\"MyConsumerId\",\"topics\":[{\"topic\":\"topic-a\",\"partitions\":[0,1],\"offsets\":[{\"partition\":0,\"offset\":0},{\"partition\":1,\"offset\":1}]},{\"topic\":\"topic-b\",\"partitions\":[0,1],\"offsets\":[{\"partition\":0,\"offset\":2},{\"partition\":1,\"offset\":3}]}],\"topicNames\":[\"topic-a\",\"topic-b\"]}";
//
//        final ConsumerGroupOffsets offsets = ConsumerGroupOffsets.newBuilder()
//            .withConsumerId("MyConsumerId")
//            .withOffset("topic-a", 0, 0)
//            .withOffset("topic-a", 1, 1)
//            .withOffset("topic-b", 0, 2)
//            .withOffset("topic-b", 1, 3)
//            .build();
//
//        // Now attempt to serialize
//        assertTrue("Should be able to serialize", objectMapper.canSerialize(ConsumerGroupOffsets.class));
//
//        // Attempt to serialize
//        final String result = objectMapper.writeValueAsString(offsets);
//
//        // Validate
//        assertEquals("Should have expected serialized value", expectedResult, result);
//    }
}