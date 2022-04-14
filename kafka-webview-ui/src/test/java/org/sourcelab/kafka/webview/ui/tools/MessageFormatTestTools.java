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

package org.sourcelab.kafka.webview.ui.tools;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helpful tools for Filters in tests.
 */
@Component
public class MessageFormatTestTools {
    private final MessageFormatRepository messageFormatRepository;

    @Autowired
    public MessageFormatTestTools(final MessageFormatRepository messageFormatRepository) {
        this.messageFormatRepository = messageFormatRepository;
    }

    /**
     * Utility for creating message formats.
     * @param name Name of the message format.
     * @return Persisted MessageFormat.
     */
    public MessageFormat createMessageFormat(final String name) {
        final MessageFormat format = new MessageFormat();
        format.setName(name);
        format.setClasspath("com.example." + name);
        format.setJar(name + ".jar");
        format.setDefaultFormat(false);
        format.setOptionParameters("{\"key\": \"value\"}");
        messageFormatRepository.save(format);

        return format;
    }

    /**
     * Creates a String message format.
     * @param name Name of the message format.
     * @return Persisted MessageFormat.
     */
    public MessageFormat createStringMessageFormat(final String name) {
        final MessageFormat format = new MessageFormat();
        format.setName(name);
        format.setClasspath(StringDeserializer.class.getName());
        format.setDefaultFormat(true);
        format.setJar("");
        format.setOptionParameters("{}");
        messageFormatRepository.save(format);

        return format;
    }
}
