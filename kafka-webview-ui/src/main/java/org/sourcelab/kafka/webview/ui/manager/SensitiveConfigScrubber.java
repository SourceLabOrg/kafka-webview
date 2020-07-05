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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslProperties;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslUtility;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for removing sensitive values from generated kafka client configuration.
 */
public class SensitiveConfigScrubber {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveConfigScrubber.class);

    private final SaslUtility saslUtility;

    @Autowired
    public SensitiveConfigScrubber(final SaslUtility saslUtility) {
        this.saslUtility = saslUtility;
    }


    /**
     * Given a Kafka Client Config, scrub any sensentivie fields from the config and return a copy.
     * @param config The config to scrub.
     * @param cluster The cluster associated with the config.
     * @return Copy of scrubbed configuration.
     */
    public Map<String, Object> filterSensitiveOptions(
        final Map<String, Object> config, final Cluster cluster
    ) {
        // Create a copy of the map
        final Map<String, Object> copy = new HashMap<>(config);

        // Filter sensitive fields
        final String[] sensitiveKeys = new String[] {
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
        };
        Arrays.stream(sensitiveKeys)
            .filter(copy::containsKey)
            .forEach((key) -> copy.put(key, "**HIDDEN**"));

        // Filter JAAS Config
        if (copy.containsKey(SaslConfigs.SASL_JAAS_CONFIG) && cluster != null) {
            final SaslProperties saslProperties = saslUtility.decodeProperties(cluster);

            String jaasConfig = (String) copy.get(SaslConfigs.SASL_JAAS_CONFIG);
            jaasConfig = jaasConfig.replaceAll(saslProperties.getPlainPassword(), "**HIDDEN**");
            copy.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
        }

        // Return copy of the map.
        return copy;
    }
}
