package com.darksci.kafkaview.manager.kafka.dto;

public class NodeDetails {
    private final int id;
    private final String host;
    private final int port;

    public NodeDetails(final int id, final String host, final int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "NodeDetails{" +
            "id=" + id +
            ", host='" + host + '\'' +
            ", port=" + port +
            '}';
    }
}
