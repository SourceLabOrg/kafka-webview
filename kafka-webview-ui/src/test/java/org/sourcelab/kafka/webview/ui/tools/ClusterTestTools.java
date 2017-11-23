package org.sourcelab.kafka.webview.ui.tools;

import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helpful tools for Clusters in tests.
 */
@Component
public class ClusterTestTools {
    private final ClusterRepository clusterRepository;

    @Autowired
    public ClusterTestTools(final ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    /**
     * Creates a cluster model entity and persists it.
     * @param name Name of the cluster.
     * @return A persisted cluster.
     */
    public Cluster createCluster(final String name) {
        final Cluster cluster = new Cluster();
        cluster.setBrokerHosts("localhost:9092");
        cluster.setSslEnabled(false);
        cluster.setName(name);
        cluster.setValid(true);
        clusterRepository.save(cluster);

        return cluster;
    }
}
