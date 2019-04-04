/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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
    // Clusters text
    public static final String NO_CLUSTERS_SETUP_TEXT = "It looks like you have no Kafka Clusters configured yet";
    public static final String CREATE_CLUSTER_TEXT = "Setup new Cluster";
    public static final String ASK_ADMIN_CREATE_CLUSTER_TEXT = "Ask an Administrator to configure a cluster.";
    public static final String CREATE_CLUSTER_LINK = "/configuration/cluster/create";

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
        return createCluster(name, "localhost:9092");
    }

    /**
     * Creates a cluster model entity and persists it.
     * @param name Name of the cluster.
     * @param brokerHosts hosts for the cluster.
     * @return A persisted cluster.
     */
    public Cluster createCluster(final String name, final String brokerHosts) {
        final Cluster cluster = new Cluster();
        cluster.setName(name);
        cluster.setBrokerHosts(brokerHosts);
        cluster.setSslEnabled(false);
        cluster.setSaslEnabled(false);
        cluster.setValid(true);
        save(cluster);

        return cluster;
    }

    /**
     * Clear all clusters from the database.
     */
    public void deleteAllClusters() {
        clusterRepository.deleteAll();
    }

    /**
     * Easy access to clusterRepository.
     * @param cluster Cluster to persist.
     */
    public void save(final Cluster cluster) {
        clusterRepository.save(cluster);
    }
}
