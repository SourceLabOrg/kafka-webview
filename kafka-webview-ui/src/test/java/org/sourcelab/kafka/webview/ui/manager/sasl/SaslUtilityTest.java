package org.sourcelab.kafka.webview.ui.manager.sasl;

import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import static org.junit.Assert.*;

public class SaslUtilityTest {

    /**
     * Tests decoding properties from a cluster.
     */
    @Test
    public void testEncodingAndDecoding() {
        final SecretManager secretManager = new SecretManager("key");
        final SaslUtility saslUtility = new SaslUtility(secretManager);

        final SaslProperties.Builder saslPropertiesBuilder = SaslProperties.newBuilder();
        saslPropertiesBuilder
            .withMechanism("PLAIN")
            .withPlainUsername("username")
            .withPlainPassword("password")
            .withJaas("Custom Jaas here");

        final SaslProperties saslProperties = saslPropertiesBuilder.build();

        // Attempt to encode
        final String encryptedProperties = saslUtility.encryptProperties(saslProperties);
        assertNotNull(encryptedProperties);

        // Build cluster instance
        final Cluster cluster = new Cluster();
        cluster.setSaslEnabled(true);
        cluster.setSaslMechanism("PLAIN");
        cluster.setSaslConfig(encryptedProperties);

        // attempt to decode
        final SaslProperties decoded = saslUtility.decodeProperties(cluster);
        assertNotNull(decoded);

        // validate
        assertEquals(saslProperties.getMechanism(), decoded.getMechanism());
        assertEquals(saslProperties.getPlainUsername(), decoded.getPlainUsername());
        assertEquals(saslProperties.getPlainPassword(), decoded.getPlainPassword());
        assertEquals(saslProperties.getJaas(), decoded.getJaas());
    }
}