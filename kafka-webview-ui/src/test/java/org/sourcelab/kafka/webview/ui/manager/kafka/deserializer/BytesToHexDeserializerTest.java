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

package org.sourcelab.kafka.webview.ui.manager.kafka.deserializer;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.kafka.common.serialization.Deserializer;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class BytesToHexDeserializerTest {

    private final Deserializer<String> deserializer = new BytesToHexDeserializer();

    /**
     * Validate the deserializer works as expected.
     */
    @Test
    @Parameters(method = "provideBytes")
    public void doTest(final byte[] bytes, final String expectedHexStr) {
        final String results = deserializer.deserialize("not-relevant", bytes);
        assertEquals("Strings should match", expectedHexStr, results);
    }

    public Object[] provideBytes() {
        return new Object[]{
            new Object[]{ "blahblahlbha".getBytes(), "626c6168626c61686c626861" },
            new Object[]{ "blahblahlbhah".getBytes(), "626c6168626c61686c62686168" },
            new Object[]{ "key1572998885260".getBytes(), "6b657931353732393938383835323630" },
            new Object[]{ "key1572998885261".getBytes(), "6b657931353732393938383835323631" },
            new Object[]{ "key1572998885255".getBytes(), "6b657931353732393938383835323535" },
            new Object[]{ "key1572998885258".getBytes(), "6b657931353732393938383835323538" },
        };
    }
}