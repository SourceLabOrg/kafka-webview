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

import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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