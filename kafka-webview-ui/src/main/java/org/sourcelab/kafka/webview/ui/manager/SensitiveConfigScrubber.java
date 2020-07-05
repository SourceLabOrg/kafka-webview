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
 *
 */
public class SensitiveConfigScrubber {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveConfigScrubber.class);

    private final SaslUtility saslUtility;

    @Autowired
    public SensitiveConfigScrubber(final SaslUtility saslUtility) {
        this.saslUtility = saslUtility;
    }


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
