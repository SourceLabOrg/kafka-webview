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

package org.sourcelab.kafka.webview.ui.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Value class for configuration values.
 */
@Component
public class AppProperties {

    @Value("${app.name}")
    private String name;

    @Value("${app.uploadPath}")
    private String uploadPath;

    @Value("${app.key}")
    private String appKey;

    @Value("${app.multiThreadedConsumer:false}")
    private boolean enableMultiThreadedConsumer;

    @Value("${app.maxConcurrentWebConsumers:32}")
    private Integer maxConcurrentWebConsumers = 32;

    @Value("${app.maxConcurrentWebSocketConsumers:100}")
    private Integer maxConcurrentWebSocketConsumers = 100;

    @Value("${app.consumerIdPrefix}")
    private String consumerIdPrefix;

    @Value("${app.requireSsl:false}")
    private boolean requireSsl = false;

    @Value("${app.avro.includeSchema:true}")
    private boolean avroIncludeSchema = true;

    /**
     * Flag read from configuration file to determine if the app should
     * enforce User authentication.
     */
    @Value("${app.user.enabled:true}")
    private boolean userAuthEnabled = true;

    /**
     * Contains LDAP related properties.
     */
    @Autowired
    private LdapAppProperties ldapProperties;

    public String getName() {
        return name;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public String getAppKey() {
        return appKey;
    }

    public Integer getMaxConcurrentWebSocketConsumers() {
        return maxConcurrentWebSocketConsumers;
    }

    public String getConsumerIdPrefix() {
        return consumerIdPrefix;
    }

    public boolean isRequireSsl() {
        return requireSsl;
    }

    public boolean isUserAuthEnabled() {
        return userAuthEnabled;
    }

    public LdapAppProperties getLdapProperties() {
        return ldapProperties;
    }

    public boolean isAvroIncludeSchema() {
        return avroIncludeSchema;
    }

    public boolean isEnableMultiThreadedConsumer() {
        return enableMultiThreadedConsumer;
    }

    public Integer getMaxConcurrentWebConsumers() {
        return maxConcurrentWebConsumers;
    }

    @Override
    public String toString() {
        return "AppProperties{"
            + "name='" + name + '\''
            + ", uploadPath='" + uploadPath + '\''
            + ", appKey='XXXXXX'"
            + ", maxConcurrentWebSocketConsumers=" + maxConcurrentWebSocketConsumers
            + ", consumerIdPrefix='" + consumerIdPrefix + '\''
            + ", requireSsl='" + requireSsl + '\''
            + ", userAuthEnabled='" + userAuthEnabled + '\''
            + '}';
    }
}
