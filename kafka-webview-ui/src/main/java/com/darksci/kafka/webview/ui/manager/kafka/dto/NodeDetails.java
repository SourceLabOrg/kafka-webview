package com.darksci.kafka.webview.ui.manager.kafka.dto;

/**
 * Represents Details about a specific node within a Cluster.
 */
public class NodeDetails {
    private final int id;
    private final String host;
    private final int port;
    private final String rack;

    /**
     * Constructor.
     */
    public NodeDetails(final int id, final String host, final int port, final String rack) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.rack = rack;
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

    public String getRack() {
        return rack;
    }

    @Override
    public String toString() {
        return "NodeDetails{"
            + "id=" + id
            + ", host='" + host + '\''
            + ", port=" + port
            + ", rack='" + rack + '\''
            + '}';
    }
}
