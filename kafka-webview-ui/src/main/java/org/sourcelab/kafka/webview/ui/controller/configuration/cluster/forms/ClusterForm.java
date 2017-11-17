/**
 * MIT License
 *
 * Copyright (c) 2017 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

    @Override
    public String toString() {
        return "ClusterForm{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", brokerHosts='" + brokerHosts + '\''
            + ", ssl=" + ssl
            + ", trustStoreFile=" + trustStoreFile
            + ", trustStoreFilename='" + trustStoreFilename + '\''
            + ", keyStoreFile=" + keyStoreFile
            + ", keyStoreFilename='" + keyStoreFilename + '\''
            + '}';
    }
}
