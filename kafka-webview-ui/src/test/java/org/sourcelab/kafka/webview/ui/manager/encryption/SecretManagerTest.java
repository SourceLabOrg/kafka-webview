/**
 * MIT License
 *
 * Copyright (c) 2017 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.manager.encryption;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SecretManagerTest {

    /**
     * By default assume no exception.
     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Simple smoke test over secret manager.
     */
    @Test
    public void doTest() throws Exception {
        final SecretManager secretManager = new SecretManager("key");

        final String input = "My Test Input String";
        final String encrypted = secretManager.encrypt(input);
        final String output = secretManager.decrypt(encrypted);
        assertEquals("Input should equal output", input, output);
    }

    /**
     * Null input into encryption = exception.
     */
    @Test
    public void doTestWithNullInput() throws Exception {
        final SecretManager secretManager = new SecretManager("key");


        expectedException.expect(NullPointerException.class);
        final String encrypted = secretManager.encrypt(null);
    }

    /**
     * Null input into decryption = null result
     */
    @Test
    public void doTestWithNullDecrypt() throws Exception {
        final SecretManager secretManager = new SecretManager("key");
        assertNull("Should have null result", secretManager.decrypt(null));
    }

    /**
     * Empty string input into decryption = empty string result
     */
    @Test
    public void doTestWithEmptyDecrypt() throws Exception {
        final SecretManager secretManager = new SecretManager("key");
        assertEquals("Should have empty string result", "", secretManager.decrypt(""));
    }

}