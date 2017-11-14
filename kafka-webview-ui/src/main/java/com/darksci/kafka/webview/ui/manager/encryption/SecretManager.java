/**
 * MIT License
 *
 * Copyright (c) 2017 Stephen Powis https://github.com/Crim/kafka-webview
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

package com.darksci.kafka.webview.ui.manager.encryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * Simple encryption manager to avoid storing plaintext secrets.
 */
public class SecretManager {
    /**
     * Passphrase used for encryption.
     */
    private final String passphrase;

    /**
     * Constructor.
     * @param passphrase Passphrase
     */
    public SecretManager(final String passphrase) {
        if (passphrase == null || passphrase.trim().isEmpty()) {
            throw new RuntimeException("App Key cannot be null or empty string!");
        }
        this.passphrase = passphrase;
    }

    /**
     * Encrypt plaintext.
     * @param str Plaintext to encrypt
     * @return Cipher text
     */
    public String encrypt(final String str) {
        if (str == null) {
            throw new NullPointerException("Argument cannot be null");
        }

        try {
            final SecureRandom random = new SecureRandom();
            final byte[] salt = new byte[16];
            random.nextBytes(salt);

            final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            final KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 128);
            final SecretKey tmp = factory.generateSecret(spec);
            final SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret);

            final AlgorithmParameters params = cipher.getParameters();
            final byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            final byte[] encryptedText = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));

            // concatenate salt + iv + ciphertext
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(salt);
            outputStream.write(iv);
            outputStream.write(encryptedText);

            // properly encode the complete cipher text
            return DatatypeConverter.printBase64Binary(outputStream.toByteArray());
        } catch (final Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    /**
     * Decrypt cipher text.
     * @param str Cipher text to decrypt.
     * @return Decrypted plain text.
     */
    public String decrypt(final String str) {
        // Handle null or empty string decryption more gracefully
        if (str == null || str.isEmpty()) {
            return str;
        }

        try {
            final byte[] ciphertext = DatatypeConverter.parseBase64Binary(str);
            if (ciphertext.length < 48) {
                return null;
            }
            final byte[] salt = Arrays.copyOfRange(ciphertext, 0, 16);
            final byte[] iv = Arrays.copyOfRange(ciphertext, 16, 32);
            final byte[] ct = Arrays.copyOfRange(ciphertext, 32, ciphertext.length);

            final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            final KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 128);
            final SecretKey tmp = factory.generateSecret(spec);
            final SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            final byte[] plaintext = cipher.doFinal(ct);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (final Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }
}
