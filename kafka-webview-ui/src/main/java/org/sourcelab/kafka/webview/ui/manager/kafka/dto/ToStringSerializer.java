package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;

import java.io.IOException;

/**
 * Uses object's toString() method to serialize.
 */
public class ToStringSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(
        final Object value,
        final JsonGenerator gen,
        final SerializerProvider serializers
    ) throws IOException {
        if (value == null) {
            gen.writeString("");
            return;
        }

        // See if we have a serializer that is NOT the unknown serializer
        final JsonSerializer serializer = serializers.findValueSerializer(value.getClass());
        if (serializer != null && !(serializer instanceof UnknownSerializer)) {
            serializer.serialize(value, gen, serializers);
            return;
        }

        // Fall back to using toString()
        gen.writeString(value.toString());
    }
}
