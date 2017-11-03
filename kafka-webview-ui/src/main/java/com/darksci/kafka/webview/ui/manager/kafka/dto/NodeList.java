package com.darksci.kafka.webview.ui.manager.kafka.dto;

import java.util.Collections;
import java.util.List;

/**
 * Represents a collection of nodes within a Cluster.
 */
public class NodeList {
    final List<NodeDetails> nodes;

    /**
     * Constructor.
     */
    public NodeList(final List<NodeDetails> nodes) {
        this.nodes = Collections.unmodifiableList(nodes);
    }

    public List<NodeDetails> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "NodeList{"
            + "nodes=" + nodes
            + '}';
    }
}
