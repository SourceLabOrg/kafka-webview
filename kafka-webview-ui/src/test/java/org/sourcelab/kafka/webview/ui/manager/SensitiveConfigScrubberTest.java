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
        cluster.setSaslEnabled(true);
        
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