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
            builder.withJaas(
                options.getOrDefault("jaas", "")
            );
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
