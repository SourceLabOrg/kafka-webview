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

package org.sourcelab.kafka.webview.ui.manager.kafka;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.junit.ClassRule;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeList;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class KafkaOperationsFactoryTest {

    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    /**
     * Test that KafkaAdminFactory can create a working AdminClient when connecting to a non-ssl cluster.
     */
    @Test
    public void smokeTestNonSslOperationsClient() throws ExecutionException, InterruptedException {
        // Create dependencies.
        final SecretManager secretManager = new SecretManager("notused");
        final KafkaAdminFactory kafkaAdminFactory = new KafkaAdminFactory("NotUsed");
        final KafkaOperationsFactory operationsFactory = new KafkaOperationsFactory(secretManager, kafkaAdminFactory);

        // Create cluster model.
        final Cluster cluster = new Cluster();
        cluster.setBrokerHosts(sharedKafkaTestResource.getKafkaConnectString());
        cluster.setSslEnabled(false);

        // Create instance
        try (final KafkaOperations kafkaOperations = operationsFactory.create(cluster, 1L)) {

            // Call method to validate things work as expected
            final NodeList nodeList = kafkaOperations.getClusterNodes();
            assertNotNull("Should have a non-null result", nodeList);
            assertFalse("Should have non-empty node", nodeList.getNodes().isEmpty());
        }
    }
}