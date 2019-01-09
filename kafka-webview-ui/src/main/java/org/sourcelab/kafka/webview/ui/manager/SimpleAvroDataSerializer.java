package org.sourcelab.kafka.webview.ui.manager;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.avro.generic.GenericData;

import java.io.IOException;

/**
 * Simple Avro to JSON serializer for Jackson.
 */
public class SimpleAvroDataSerializer extends JsonSerializer<GenericData.Record> {
    private final boolean includeSchema;

    /**
     * No arg constructor.  Default to including the schema.
     */
    public SimpleAvroDataSerializer() {
        this(true);
    }

    /**
     * Constructor.
     * @param includeSchema Should the schema be included.
     */
    public SimpleAvroDataSerializer(final boolean includeSchema) {
        this.includeSchema = includeSchema;
    }

    @Override
    public void serialize(final GenericData.Record value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        if (includeSchema) {
            writeIncludingSchema(value, gen);
        } else {
            writeValueOnly(value, gen);
        }
    }

    private void writeIncludingSchema(final GenericData.Record value, final JsonGenerator gen) throws IOException {
        // Start new object.
        gen.writeStartObject();

        // Write value
        gen.writeFieldName("value");
        gen.writeRawValue(value.toString());

        // Write schema
        gen.writeFieldName("schema");
        gen.writeRawValue(value.getSchema().toString());

        // End object
        gen.writeEndObject();
    }

    private void writeValueOnly(final GenericData.Record value, final JsonGenerator gen) throws IOException {
        gen.writeRawValue(value.toString());
    }
}
