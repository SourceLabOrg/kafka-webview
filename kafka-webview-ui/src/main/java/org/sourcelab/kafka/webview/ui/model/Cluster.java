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

package org.sourcelab.kafka.webview.ui.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Cluster Entity.
 */
@Entity
public class Cluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String brokerHosts;

    @Column(nullable = false)
    private boolean isSslEnabled;

    private String trustStoreFile;

    /**
     * Stored encrypted.
     */
    private String trustStorePassword;

    private String keyStoreFile;

    /**
     * Stored encrypted.
     */
    private String keyStorePassword;

    @Column(nullable = false)
    private boolean isSaslEnabled;

    /**
     * Stores the name of the SASL mechanism.
     */
    private String saslMechanism;

    /**
     * JSON representation of sasl configuration options.
     * Typically with the format of:
     * { username: "", password: "", jaas: "" }
     *
     * Since this contains sensitive data, this value is stored encrypted.
     */
    private String saslConfig;

    @Column(nullable = false)
    private boolean isValid;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getBrokerHosts() {
        return brokerHosts;
    }

    public void setBrokerHosts(final String brokerHosts) {
        this.brokerHosts = brokerHosts;
    }

    public boolean isSslEnabled() {
        return isSslEnabled;
    }

    public void setSslEnabled(final boolean useSsl) {
        this.isSslEnabled = useSsl;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(final String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(final String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(final String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(final String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(final boolean valid) {
        isValid = valid;
    }

    public boolean isSaslEnabled() {
        return isSaslEnabled;
    }

    public void setSaslEnabled(final boolean saslEnabled) {
        isSaslEnabled = saslEnabled;
    }

    public String getSaslMechanism() {
        return saslMechanism;
    }

    public void setSaslMechanism(final String saslMechanism) {
        this.saslMechanism = saslMechanism;
    }

    public String getSaslConfig() {
        return saslConfig;
    }

    public void setSaslConfig(final String saslConfig) {
        this.saslConfig = saslConfig;
    }

    @Override
    public String toString() {
        return "Cluster{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", brokerHosts='" + brokerHosts + '\''
            + ", isSslEnabled=" + isSslEnabled
            + ", trustStoreFile='" + trustStoreFile + '\''
            + ", keyStoreFile='" + keyStoreFile + '\''
            + ", isSaslEnabled=" + isSaslEnabled
            + ", saslMechanism='" + saslMechanism + '\''
            + ", isValid=" + isValid
            + '}';
    }
}
