package org.sourcelab.kafka.webview.ui.manager;

import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.junit.Before;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslProperties;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslUtility;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;

public class SensitiveConfigScrubberTest {

    /**
     * Instance under test.
     */
    private SensitiveConfigScrubber scrubber;

    /**
     * Dependency.
     */
    private SaslUtility saslUtility;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        this.saslUtility = new SaslUtility(new SecretManager("NotARealSecret"));
        this.scrubber = new SensitiveConfigScrubber(saslUtility);
    }

    /**
     * Smoke test filterSensitiveOptions method.
     */
    @Test
    public void smokeTest() {
        final String secretPhrase = "secret";

        final SaslProperties saslProperties = new SaslProperties("Username", secretPhrase, "Anything", "Something");

        // Create cluster
        final Cluster cluster = new Cluster();
        cluster.setName("My Test Cluster");
        cluster.setSaslConfig(saslUtility.encryptProperties(saslProperties));


        // Create a mock config
        final Map<String, Object> mockConfig = new HashMap<>();
        mockConfig.put("Key1", "Value1");
        mockConfig.put("Key2", "Value2");

        // Add "sensitive" fields
        mockConfig.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, secretPhrase);
        mockConfig.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, secretPhrase);
        mockConfig.put(SaslConfigs.SASL_JAAS_CONFIG, "Anything anything " + secretPhrase + " something");

        // Call method under test
        final Map<String, Object> scrubbed = scrubber.filterSensitiveOptions(mockConfig, cluster);

        // Verify we have a new instance.
        assertNotSame("Should be different instances", scrubbed, mockConfig);

        // Verify keys
        assertEquals(scrubbed.get("Key1"), mockConfig.get("Key1"));
        assertEquals(scrubbed.get("Key2"), mockConfig.get("Key2"));

        // Sensitive fields no longer match
        assertNotEquals(scrubbed.get(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG), mockConfig.get(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
        assertEquals("**HIDDEN**", scrubbed.get(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
        assertNotEquals(scrubbed.get(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG), mockConfig.get(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
        assertEquals("**HIDDEN**", scrubbed.get(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));

        assertNotEquals(scrubbed.get(SaslConfigs.SASL_JAAS_CONFIG), mockConfig.get(SaslConfigs.SASL_JAAS_CONFIG));
        assertFalse(
            ((String)scrubbed.get(SaslConfigs.SASL_JAAS_CONFIG)).contains(secretPhrase)
        );
    }
}