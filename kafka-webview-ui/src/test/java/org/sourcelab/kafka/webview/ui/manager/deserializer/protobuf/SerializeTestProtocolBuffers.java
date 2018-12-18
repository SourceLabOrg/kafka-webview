package org.sourcelab.kafka.webview.ui.manager.deserializer.protobuf;

import com.google.protobuf.ByteString;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResult;

public class SerializeTestProtocolBuffers {

    @Test
    public void testProtocolBufferInKafkaResult() {
        final TestProtocolBuffers.ProtoA.Builder builder = TestProtocolBuffers.ProtoA.newBuilder();
        builder
            .setArrayField(0,"Value0")
            .setArrayField(1,"Value1")
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
        final KafkaResult result = new KafkaResult(0, 123L, 123L, "Key Value", protoA);

        // Attempt to serialize it.

    }
}
