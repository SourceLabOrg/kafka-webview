package com.darksci.kafka.webview.ui.model;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String brokerHosts;

    @Column(nullable = false)
    private boolean isSslEnabled;

    private String trustStoreFile;

    // TODO Encrypt this value?
    private String trustStorePassword;

    private String keyStoreFile;

    // TODO Encrypt this value?
    private String keyStorePassword;

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

    @Override
    public String toString() {
        return "Cluster{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", brokerHosts='" + brokerHosts + '\'' +
            ", isSslEnabled=" + isSslEnabled +
            ", trustStoreFile='" + trustStoreFile + '\'' +
            ", keyStoreFile='" + keyStoreFile + '\'' +
            ", isValid=" + isValid +
            '}';
    }
}
