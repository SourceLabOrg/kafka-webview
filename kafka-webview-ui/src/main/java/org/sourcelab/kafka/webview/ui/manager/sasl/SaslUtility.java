/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.manager.sasl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for dealing with SASL properties stored on cluster.sasl_config field.
 */
public class SaslUtility {

    /**
     * For decrypting secrets.
     */
    private final SecretManager secretManager;

    /**
     * For parsing JSON.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     * @param secretManager Handles encryption/decryption logic.
     */
    public SaslUtility(final SecretManager secretManager) {
        this.secretManager = secretManager;
    }

    /**
     * Given a cluster instance, return a value object containing the decrypted/decoded sasl properties
     * associated with it.
     * @param cluster cluster instsance to decode properties from.
     * @return Value object representing the properties.
     */
    public SaslProperties decodeProperties(final Cluster cluster) {
        if (cluster == null) {
            throw new RuntimeException("Invalid cluster passed!");
        }

        // Create builder.
        final SaslProperties.Builder builder = SaslProperties.newBuilder();
        if (cluster.getSaslMechanism() != null) {
            builder.withMechanism(cluster.getSaslMechanism());
        }

        if (!cluster.isSaslEnabled()) {
            // return empty properties?
            return builder.build();
        }

        // Decrypt json
        final String configJsonStr;
        if (cluster.getSaslConfig() == null) {
            configJsonStr = "{}";
        } else {
            configJsonStr = secretManager.decrypt(cluster.getSaslConfig());
        }

        // Parse options from string
        try {
            final Map<String, String> options = mapper.readValue(configJsonStr, Map.class);

            builder.withPlainUsername(
                options.getOrDefault("username", "")
            );
            builder.withPlainPassword(
                options.getOrDefault("password", "")
            );

            // Determine if we have a custom defined JAAS
            final String customJaas = options.getOrDefault("jaas", "").trim();
            if (!customJaas.isEmpty()) {
                // Use it as is.
                builder.withJaas(customJaas);
            } else if ("PLAIN".equals(cluster.getSaslMechanism())) {
                // Build a standard plain JAAS?
                final String jaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required\n"
                    + "username=\"" + options.getOrDefault("username", "") + "\"\n"
                    + "password=\"" + options.getOrDefault("password", "") + "\";";

                builder.withJaas(jaasConfig);
            }
        } catch (final IOException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }

        return builder.build();
    }

    /**
     * Given a set of SaslProperties, convert to JSON and encrypt the value.
     * @param saslProperties Properties to encrypt.
     * @return JSON + Encrypted value.
     */
    public String encryptProperties(final SaslProperties saslProperties) {
        if (saslProperties == null) {
            throw new RuntimeException("Invalid saslProperties passed!");
        }

        // Build map
        final Map<String, String> options = new HashMap<>();
        options.put("username", saslProperties.getPlainUsername());
        options.put("password", saslProperties.getPlainPassword());
        options.put("jaas", saslProperties.getJaas());

        try {
            // Convert to plaintext json
            final String jsonStr = mapper.writeValueAsString(options);

            // Encrypt the value
            return secretManager.encrypt(jsonStr);
        } catch (final JsonProcessingException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }
}
