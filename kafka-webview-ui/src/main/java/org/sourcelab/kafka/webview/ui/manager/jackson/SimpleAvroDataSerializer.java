/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.manager.jackson;

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
