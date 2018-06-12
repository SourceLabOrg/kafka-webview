package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class KafkaResultTest {
    /**
     * Verify that we serialize using jackson appropriately using a 'known'/string value.
     */
    @Test
    public void testSerializationWithStringType() throws JsonProcessingException {
        final int partition = 10;
        final long offset = 1234L;
        final long timestamp = 4321L;
        final Object key = "key";
        final Object value = "value";

        // Define our expected output.
        final String expectedResult = "{"
            + "\"partition\":" + partition + ","
            + "\"offset\":" + offset + ","
            + "\"timestamp\":" + timestamp + ","
            + "\"key\":\"key\","
            + "\"value\":\"value\""
            + "}";

        final KafkaResult kafkaResult = new KafkaResult(
            partition,
            offset,
            timestamp,
            key,
            value
        );

        // Now attempt to serialize
        final ObjectMapper objectMapper = new ObjectMapper();
        assertTrue("Should be able to serialize", objectMapper.canSerialize(KafkaResult.class));

        // Attempt to serialize
        final String result = objectMapper.writeValueAsString(kafkaResult);

        // Validate
        assertEquals("Should have expected serialized value", expectedResult, result);
    }

    /**
     * Verify that we serialize using jackson appropriately using a 'known'/long/int value.
     */
    @Test
    public void testSerializationWithLongType() throws JsonProcessingException {
        final int partition = 10;
        final long offset = 1234L;
        final long timestamp = 4321L;
        final Object key = 10L;
        final Object value = 12;

        // Define our expected output.
        final String expectedResult = "{"
            + "\"partition\":" + partition + ","
            + "\"offset\":" + offset + ","
            + "\"timestamp\":" + timestamp + ","
            + "\"key\":10,"
            + "\"value\":12"
            + "}";

        final KafkaResult kafkaResult = new KafkaResult(
            partition,
            offset,
            timestamp,
            key,
            value
        );

        // Now attempt to serialize
        final ObjectMapper objectMapper = new ObjectMapper();
        assertTrue("Should be able to serialize", objectMapper.canSerialize(KafkaResult.class));

        // Attempt to serialize
        final String result = objectMapper.writeValueAsString(kafkaResult);

        // Validate
        assertEquals("Should have expected serialized value", expectedResult, result);
    }

    /**
     * Verify that we serialize using jackson appropriately using an 'unknown' or 'unregistered' type.
     */
    @Test
    public void testSerializationWithCustomObjectType() throws JsonProcessingException {
        final int partition = 10;
        final long offset = 1234L;
        final long timestamp = 4321L;
        final Object key = new TestObject("key");
        final Object value = new TestObject("value");

        // Define our expected output.
        final String expectedResult = "{"
            + "\"partition\":" + partition + ","
            + "\"offset\":" + offset + ","
            + "\"timestamp\":" + timestamp + ","
            + "\"key\":\"TestObject:key\","
            + "\"value\":\"TestObject:value\""
            + "}";

        final KafkaResult kafkaResult = new KafkaResult(
            partition,
            offset,
            timestamp,
            key,
            value
        );

        // Now attempt to serialize
        final ObjectMapper objectMapper = new ObjectMapper();
        assertTrue("Should be able to serialize", objectMapper.canSerialize(KafkaResult.class));

        // Attempt to serialize
        final String result = objectMapper.writeValueAsString(kafkaResult);

        // Validate
        assertEquals("Should have expected serialized value", expectedResult, result);
    }

    /**
     * Test class.  This should get serialized using its toString() method.
     */
    public static class TestObject {
        final Object value;

        public TestObject(final Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "TestObject:" + value.toString();
        }
    }
}