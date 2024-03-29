/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResult;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResults;
import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class WebKafkaConsumerFactoryTest {

    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    /**
     * Shared executor service for multi-threaded consuming.
     */
    private static ExecutorService executorService;

    private String topic1;
    private String topic2;

    /**
     * Create executor service.
     */
    @BeforeClass
    public static void setup() {
        executorService = Executors.newFixedThreadPool(10);
    }

    /**
     * Cleanup executor service after finished.
     */
    @AfterClass
    public static void tearDown() {
        executorService.shutdownNow();
    }

    @Before
    public void beforeTest() {
        this.topic1 = "FirstTopic" + System.currentTimeMillis();
        this.topic2 = "SecondTopic" + System.currentTimeMillis();

        // Create topics
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topic1, 2, (short) 1);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topic2, 2, (short) 1);

        // Publish data into topics
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topic1, 0);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topic1, 1);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topic2, 0);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topic2, 1);
    }

    public Object[] provideFactoryInstances() {
        return new Object[]{
            new Object[]{createDefaultFactory()},
            new Object[]{createMultiThreadedFactory()},
        };
    }

    /**
     * Smoke test over webClient, using no record filters or partition filtering.
     */
    @Test
    @Parameters(method = "provideFactoryInstances")
    public void smokeTestWebClient_noFilters_allPartitions(final WebKafkaConsumerFactory factory) {
        final int resultsPerPartition = 5;

        // Create default view.
        final View view = createDefaultView(topic1);

        // Set results per partition to 5
        view.setResultsPerPartition(resultsPerPartition);

        // Create SessionId
        final SessionIdentifier sessionId = SessionIdentifier.newWebIdentifier(12L, "MySession");

        // Ok lets see what happens
        try (final WebKafkaConsumer webKafkaConsumer = factory.createWebClient(view, new ArrayList<>(), sessionId)) {
            // Validate we got something back
            assertNotNull(webKafkaConsumer);

            final List<KafkaResult> results = consumeAllResults(webKafkaConsumer, resultsPerPartition);
            assertEquals( "Should have a total of 20 records", 20, results.size());

            // Validate is from partitions 0 and 1
            for (final KafkaResult kafkaResult: results) {
                assertTrue("Should be from partition 0 or 1", kafkaResult.getPartition() == 0 || kafkaResult.getPartition() == 1);
            }
        }
    }

    /**
     * Smoke test over webClient, using no record filters but only a single partition.
     */
    @Test
    @Parameters(method = "provideFactoryInstances")
    public void smokeTestWebClient_noFilters_partitionFilter(final WebKafkaConsumerFactory factory) {
        final int resultsPerPartition = 5;

        // Create default view.
        final View view = createDefaultView(topic1);

        // Set results per partition to 5
        view.setResultsPerPartition(resultsPerPartition);

        // Set to only read partition 1
        view.setPartitions("1");

        // Create SessionId
        final SessionIdentifier sessionId = SessionIdentifier.newWebIdentifier(12L, "MySession");

        // Ok lets see what happens
        try (final WebKafkaConsumer webKafkaConsumer = factory.createWebClient(view, new ArrayList<>(), sessionId)) {
            // Validate we got something back
            assertNotNull(webKafkaConsumer);

            // Consume everything
            final List<KafkaResult> results = consumeAllResults(webKafkaConsumer, resultsPerPartition);
            assertEquals("Should have 10 records", 10, results.size());

            // Validate is from partition 1 only
            for (final KafkaResult kafkaResult: results) {
                assertEquals("Should be from partition 1", 1, kafkaResult.getPartition());
            }
        }
    }

    private List<KafkaResult> consumeAllResults(
        final WebKafkaConsumer webKafkaConsumer,
        final int resultsPerPartition) {

        final List<KafkaResult> allResults = new ArrayList<>();

        // Attempt to read from head.
        webKafkaConsumer.toHead();

        // Request
        KafkaResults kafkaResults = webKafkaConsumer.consumePerPartition();
        while (kafkaResults.getNumberOfRecords() > 0) {
            final Map<Integer, Integer> countsPerPartition = new HashMap<>();
            for (final KafkaResult kafkaResult : kafkaResults.getResults()) {
                final int partition = kafkaResult.getPartition();
                int previousCount = 0;
                if (countsPerPartition.containsKey(partition)) {
                    previousCount = countsPerPartition.get(partition);
                }
                countsPerPartition.put(partition, previousCount + 1);
            }
            for (final Map.Entry<Integer, Integer> entry : countsPerPartition.entrySet()) {
                final int partitionId = entry.getKey();
                final int count = entry.getValue();
                assertEquals("Should have " + resultsPerPartition + " from partition" + partitionId, resultsPerPartition, count);
            }
            allResults.addAll(kafkaResults.getResults());

            kafkaResults = webKafkaConsumer.consumePerPartition();
        }
        return allResults;
    }

    private WebKafkaConsumerFactory createDefaultFactory() {
        final PluginFactory<Deserializer> deserializerPluginFactory = new PluginFactory<>("not/used", Deserializer.class);
        final PluginFactory<RecordFilter> filterPluginFactoryPluginFactory = new PluginFactory<>("not/used", RecordFilter.class);
        final SecretManager secretManager = new SecretManager("Passphrase");
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory(
            new KafkaClientConfigUtil("not/used", "MyPrefix")
        );

        return new WebKafkaConsumerFactory(
            deserializerPluginFactory,
            filterPluginFactoryPluginFactory,
            secretManager,
            kafkaConsumerFactory,
            null
        );
    }

    private WebKafkaConsumerFactory createMultiThreadedFactory() {
        final PluginFactory<Deserializer> deserializerPluginFactory = new PluginFactory<>("not/used", Deserializer.class);
        final PluginFactory<RecordFilter> filterPluginFactoryPluginFactory = new PluginFactory<>("not/used", RecordFilter.class);
        final SecretManager secretManager = new SecretManager("Passphrase");
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory(
            new KafkaClientConfigUtil("not/used", "MyPrefix")
        );

        return new WebKafkaConsumerFactory(
            deserializerPluginFactory,
            filterPluginFactoryPluginFactory,
            secretManager,
            kafkaConsumerFactory,
            executorService
        );
    }

    private View createDefaultView(final String topic) {
        // Create a cluster
        final Cluster cluster = new Cluster();
        cluster.setName("My Fancy Cluster");
        cluster.setBrokerHosts(sharedKafkaTestResource.getKafkaConnectString());
        cluster.setSslEnabled(false);

        // Create MessageFormat for strings
        final MessageFormat stringFormat = new MessageFormat();
        stringFormat.setName("Key message format");
        stringFormat.setDefaultFormat(true);
        stringFormat.setClasspath(StringDeserializer.class.getCanonicalName());

        // No filters by default

        // Create a View
        final View view = new View();
        view.setName("My Wonderful View");
        view.setTopic(topic);
        view.setCluster(cluster);
        view.setKeyMessageFormat(stringFormat);
        view.setValueMessageFormat(stringFormat);
        // All partitions by default
        view.setPartitions("");
        view.setResultsPerPartition(5);

        return view;
    }

    /**
     * Test implementation, skip configured partition.
     */
    public class TestPartitionFilter implements RecordFilter {

        private int partition = 0;

        @Override
        public Set<String> getOptionNames() {
            final Set<String> options = new HashSet<>();
            options.add("partition");

            return options;
        }

        @Override
        public void configure(final Map<String, ?> consumerConfigs, final Map<String, String> filterOptions) {
            partition = Integer.valueOf(filterOptions.get("partition"));
        }

        @Override
        public boolean includeRecord(final String topic, final int partition, final long offset, final Object key, final Object value) {
            return partition != partition;
        }

        @Override
        public void close() {
            // Not used.
        }
    }
}