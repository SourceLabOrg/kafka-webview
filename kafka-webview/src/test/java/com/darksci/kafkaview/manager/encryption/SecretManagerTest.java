package com.darksci.kafkaview.manager.encryption;

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