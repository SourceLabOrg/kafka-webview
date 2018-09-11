/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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
        save(cluster);

        return cluster;
    }

    /**
     * Easy access to clusterRepository.
     * @param cluster Cluster to persist.
     */
    public void save(final Cluster cluster) {
        clusterRepository.save(cluster);
    }
}
