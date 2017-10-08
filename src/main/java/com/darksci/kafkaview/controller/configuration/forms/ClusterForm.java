package com.darksci.kafkaview.controller.configuration.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ClusterForm {
    @NotNull(message = "Enter a unique name")
    @Size(min = 2, max = 255)
    private String name;

    @NotNull(message = "Enter kafka broker hosts")
    @Size(min = 2, max = 255)
    private String brokerHosts;

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

    @Override
    public String toString() {
        return "ClusterForm{" +
            "name='" + name + '\'' +
            ", brokerHosts='" + brokerHosts + '\'' +
            '}';
    }
}
