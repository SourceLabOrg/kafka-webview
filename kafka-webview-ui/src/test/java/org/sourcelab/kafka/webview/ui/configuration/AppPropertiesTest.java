/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.configuration;

import org.junit.Test;
import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;

public class AppPropertiesTest {

    /**
     * Validate toString never spits out sensitive fields.
     */
    @Test
    public void testToString() throws NoSuchFieldException, IllegalAccessException {
        final String expectedSecret = "MySuperSecretKey";

        // Create app Properties instance
        final AppProperties appProperties = new AppProperties();

        // Jump through hoops to set property
        final Field field = appProperties.getClass().getDeclaredField("appKey");
        field.setAccessible(true);
        field.set(appProperties, expectedSecret);

        final String result = appProperties.toString();
        assertFalse("Should not contain our sensitive field", result.contains(expectedSecret));
    }
}