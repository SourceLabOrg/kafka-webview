package com.darksci.kafkaview.model;

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

    @Column(nullable = false, unique = false)
    private String brokerHosts;

    @Column(nullable = false, unique = false)
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
            ", isValid=" + isValid +
            '}';
    }
}
