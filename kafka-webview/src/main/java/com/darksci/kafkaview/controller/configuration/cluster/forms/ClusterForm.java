package com.darksci.kafkaview.controller.configuration.cluster.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ClusterForm {
    private Long id = null;

    @NotNull(message = "Enter a unique name")
    @Size(min = 2, max = 255)
    private String name;

    @NotNull(message = "Enter kafka broker hosts")
    @Size(min = 2)
    private String brokerHosts;

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

    public boolean exists() {
        return getId() != null;
    }

    @Override
    public String toString() {
        return "ClusterForm{" +
            "name='" + name + '\'' +
            ", brokerHosts='" + brokerHosts + '\'' +
            '}';
    }
}
