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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.manager.deserializer.protobuf.TestProtocolBuffers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class KafkaResultTest {

    /**
     * Reference to springboot's configured object mapper.
     */
    @Autowired
    private ObjectMapper objectMapper;

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
        assertTrue("Should be able to serialize", objectMapper.canSerialize(KafkaResult.class));

        // Attempt to serialize
        final String result = objectMapper.writeValueAsString(kafkaResult);

        // Validate
        assertEquals("Should have expected serialized value", expectedResult, result);
    }

    /**
     * Test using a protocol buffer.
     */
    @Test
    public void testProtocolBufferInKafkaResult() throws JsonProcessingException {
        final TestProtocolBuffers.ProtoA.Builder builder = TestProtocolBuffers.ProtoA.newBuilder();
        builder
            .addArrayField("Value0")
            .addArrayField("Value1")
            .setBoolField(true)
            .setBytesField(ByteString.copyFromUtf8("BytesValueString"))
            .setCustomEnumFieldValue(TestProtocolBuffers.ProtoA.CustomEnum.D.getNumber())
            .setDoubleField(123.123D)
            .setEmbeddedField(TestProtocolBuffers.ProtoA.Embedded.newBuilder().setName("EmbeddedField Name").build())
            .setFixed32Field(32)
            .setFixed64Field(123456789L)
            .setFixed64Field(1234567890L)
            .setFloatField(321.321F)
            .setInt32Field(32)
            .setInt64Field(64L)
            .setSint32Field(32)
            .setSint64Field(64L)
            .setStringField("String Value")
            .setUint32Field(32)
            .setUint64Field(64);
        final TestProtocolBuffers.ProtoA protoA = builder.build();

        // Create new kafka result instance using protocol buffer
        final KafkaResult kafkaResult = new KafkaResult(0, 123L, 123L, "Key Value", protoA);

        // Now attempt to serialize
        assertTrue("Should be able to serialize", objectMapper.canSerialize(KafkaResult.class));
        assertTrue("Should be able to serialize", objectMapper.canSerialize(TestProtocolBuffers.ProtoA.class));

        // Attempt to serialize
        final String result = objectMapper.writeValueAsString(kafkaResult);

        final String expectedValue = "{\"partition\":0,\"offset\":123,\"timestamp\":123,\"key\":\"Key Value\",\"value\":{\"doubleField\":123.123,\"floatField\":321.321,\"int32Field\":32,\"int64Field\":64,\"uint32Field\":32,\"uint64Field\":64,\"sint32Field\":32,\"sint64Field\":64,\"fixed32Field\":32,\"fixed64Field\":1234567890,\"sfixed32Field\":0,\"sfixed64Field\":0,\"boolField\":true,\"stringField\":\"String Value\",\"bytesField\":\"Qnl0ZXNWYWx1ZVN0cmluZw==\",\"customEnumField\":\"D\",\"arrayField\":[\"Value0\",\"Value1\"],\"embeddedField\":{\"name\":\"EmbeddedField Name\"},\"mapField\":{}}}";
        assertEquals(expectedValue, result);
    }

    /**
     * Test class.  This should get serialized using its toString() method.
     */
    public static class TestObject {
        private final Object value;

        public TestObject(final Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "TestObject:" + value.toString();
        }
    }
}