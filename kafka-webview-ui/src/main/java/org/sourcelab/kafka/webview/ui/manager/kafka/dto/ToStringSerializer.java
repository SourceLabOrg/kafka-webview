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

package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;

import java.io.IOException;

/**
 * Attempts to serialize using Jackson, if that fails, falls back to using toString.
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
        gen.writeString(value.toString());
    }
}
