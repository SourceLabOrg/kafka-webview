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

package org.sourcelab.kafka.webview.ui.controller.configuration.cluster.forms;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents the form for creating/updating the Cluster entity.
 */
public class ClusterForm {
    private Long id = null;

    @NotNull(message = "Enter a unique name")
    @Size(min = 2, max = 255)
    private String name;

    @NotNull(message = "Enter kafka broker hosts")
    @Size(min = 2)
    private String brokerHosts;

    // SSL Options
    private Boolean ssl = false;

    private MultipartFile trustStoreFile;
    private String trustStoreFilename;

    private String trustStorePassword;

    private MultipartFile keyStoreFile;
    private String keyStoreFilename;

    private String keyStorePassword;

    // SASL Options
    /**
     * Global on/off switch for SASL.
     */
    private Boolean sasl = false;

    /**
     * Defines which SASL mechanism to use.
     * If has a value of "other" then refer to value stored in saslCustomMechanism.
     */
    private String saslMechanism = "PLAIN";

    /**
     * If user selects to use a custom SASL mechanism,
     * the value here is used.
     */
    private String saslCustomMechanism;

    /**
     * For SASL mechanism PLAIN, the username to use.
     */
    private String saslUsername;

    /**
     * For SASL mechanism PLAIN, the password to use.
     */
    private String saslPassword;

    /**
     * If user selects to use a custom JAAS configuration,
     * this is the value of that.
     */
    private String saslCustomJaas;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
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

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(final Boolean ssl) {
        this.ssl = ssl;
    }

    public MultipartFile getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(final MultipartFile trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    /**
     * @return filename for the truststore.
     */
    public String getTrustStoreFilename() {
        if (getTrustStoreFile() != null && !getTrustStoreFile().isEmpty()) {
            return trustStoreFilename;
        } else {
            return trustStoreFilename;
        }
    }

    public void setTrustStoreFilename(final String trustStoreFilename) {
        this.trustStoreFilename = trustStoreFilename;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(final String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public MultipartFile getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(final MultipartFile keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    /**
     * @return filename for the keystore.
     */
    public String getKeyStoreFilename() {
        if (getKeyStoreFile() != null && !getKeyStoreFile().isEmpty()) {
            return getKeyStoreFile().getOriginalFilename();
        } else {
            return keyStoreFilename;
        }
    }

    public void setKeyStoreFilename(final String keyStoreFilename) {
        this.keyStoreFilename = keyStoreFilename;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(final String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public boolean exists() {
        return getId() != null;
    }

    public Boolean getSasl() {
        return sasl;
    }

    public void setSasl(final Boolean sasl) {
        this.sasl = sasl;
    }

    public String getSaslMechanism() {
        return saslMechanism;
    }

    public void setSaslMechanism(final String saslMechanism) {
        this.saslMechanism = saslMechanism;
    }

    public String getSaslCustomMechanism() {
        return saslCustomMechanism;
    }

    public void setSaslCustomMechanism(final String saslCustomMechanism) {
        this.saslCustomMechanism = saslCustomMechanism;
    }

    public String getSaslUsername() {
        return saslUsername;
    }

    public void setSaslUsername(final String saslUsername) {
        this.saslUsername = saslUsername;
    }

    public String getSaslPassword() {
        return saslPassword;
    }

    public void setSaslPassword(final String saslPassword) {
        this.saslPassword = saslPassword;
    }

    public String getSaslCustomJaas() {
        return saslCustomJaas;
    }

    public void setSaslCustomJaas(final String saslCustomJaas) {
        this.saslCustomJaas = saslCustomJaas;
    }

    public boolean isCustomSaslMechanism() {
        return "custom".equals(saslMechanism);
    }

    public boolean isPlainSaslMechanism() {
        return "PLAIN".equals(saslMechanism);
    }

    @Override
    public String toString() {
        return "ClusterForm{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", brokerHosts='" + brokerHosts + '\''
            + ", ssl=" + ssl
            + ", trustStoreFilename='" + trustStoreFilename + '\''
            + ", keyStoreFilename='" + keyStoreFilename + '\''
            + ", sasl=" + sasl
            + ", saslMechanism='" + saslMechanism + '\''
            + ", saslCustomMechanism='" + saslCustomMechanism + '\''
            + '}';
    }
}
