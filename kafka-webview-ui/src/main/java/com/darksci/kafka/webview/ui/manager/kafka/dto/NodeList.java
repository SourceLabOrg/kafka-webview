package com.darksci.kafka.webview.ui.manager.kafka.dto;

import java.util.Collections;
import java.util.List;

public class NodeList {
    final List<NodeDetails> nodes;

    public NodeList(final List<NodeDetails> nodes) {
        this.nodes = Collections.unmodifiableList(nodes);
    }

    public List<NodeDetails> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "NodeList{" +
            "nodes=" + nodes +
            '}';
    }
}
