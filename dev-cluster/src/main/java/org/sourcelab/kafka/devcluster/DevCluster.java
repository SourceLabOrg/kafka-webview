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

package org.sourcelab.kafka.devcluster;

import com.salesforce.kafka.test.KafkaTestCluster;
import com.salesforce.kafka.test.KafkaTestUtils;
import com.salesforce.kafka.test.listeners.BrokerListener;
import com.salesforce.kafka.test.listeners.PlainListener;
import com.salesforce.kafka.test.listeners.SaslPlainListener;
import com.salesforce.kafka.test.listeners.SaslSslListener;
import com.salesforce.kafka.test.listeners.SslListener;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.Properties;

/**
 * Simple applicatin for firing up a Kafka cluster of 1 or more nodes.  This exists solely
 * because standing up a real multi-node kafka cluster is more complicated than putting this together.
 *
 * Not intended for external consumption.
 */
public class DevCluster {
    private static final Logger logger = LoggerFactory.getLogger(DevCluster.class);

    /**
     * Main entry point
     * @param args command line args.
     */
    public static void main(final String[] args) throws Exception {
        // Parse command line arguments
        final CommandLine cmd = parseArguments(args);

        // Right now we accept one parameter, the number of nodes in the cluster.
        final int clusterSize = Integer.parseInt(cmd.getOptionValue("size"));
        logger.info("Starting up kafka cluster with {} brokers", clusterSize);

        // Default to plaintext listener.
        BrokerListener listener = new PlainListener();

        final URL trustStore = DevCluster.class.getClassLoader().getResource("kafka.truststore.jks");
        final URL keyStore = DevCluster.class.getClassLoader().getResource("kafka.keystore.jks");

        final Properties properties = new Properties();
        if (cmd.hasOption("sasl") && cmd.hasOption("ssl")) {
            listener = new SaslSslListener()
                // SSL Options
                .withClientAuthRequired()
                .withTrustStoreLocation(trustStore.getFile())
                .withTrustStorePassword("password")
                .withKeyStoreLocation(keyStore.getFile())
                .withKeyStorePassword("password")
                .withKeyPassword("password")
                // SASL Options.
                .withUsername("kafkaclient")
                .withPassword("client-secret");
        } else if (cmd.hasOption("sasl")) {
            listener = new SaslPlainListener()
                .withUsername("kafkaclient")
                .withPassword("client-secret");
        } else if (cmd.hasOption("ssl")) {
            listener = new SslListener()
                .withClientAuthRequired()
                .withTrustStoreLocation(trustStore.getFile())
                .withTrustStorePassword("password")
                .withKeyStoreLocation(keyStore.getFile())
                .withKeyStorePassword("password")
                .withKeyPassword("password");
        }

        // Create a test cluster
        final KafkaTestCluster kafkaTestCluster = new KafkaTestCluster(
            clusterSize,
            properties,
            Collections.singletonList(listener)
        );

        // Start the cluster.
        kafkaTestCluster.start();

        // Create topics
        String[] topicNames = null;
        if (cmd.hasOption("topic")) {
            topicNames = cmd.getOptionValues("topic");

            for (final String topicName : topicNames) {
                final KafkaTestUtils utils = new KafkaTestUtils(kafkaTestCluster);
                utils.createTopic(topicName, clusterSize, (short) clusterSize);

                // Publish some data into that topic
                for (int partition = 0; partition < clusterSize; partition++) {
                    utils.produceRecords(1000, topicName, partition);
                }
            }
        }

        // Log how to connect to cluster brokers.
        kafkaTestCluster
            .getKafkaBrokers()
            .stream()
            .forEach((broker) -> logger.info("Started broker with Id {} at {}", broker.getBrokerId(), broker.getConnectString()));

        // Log topic names created.
        if (topicNames != null) {
            logger.info("Created topics: {}", String.join(", ", topicNames));
        }

        // Log cluster connect string.
        logger.info("Cluster started at: {}", kafkaTestCluster.getKafkaConnectString());

        // Wait forever.
        Thread.currentThread().join();
    }

    private static CommandLine parseArguments(final String[] args) throws ParseException {
        // create Options object
        final Options options = new Options();

        // add number of brokers
        options.addOption(Option.builder("size")
            .desc("Number of brokers to start")
            .required()
            .hasArg()
            .type(Integer.class)
            .build()
        );

        options.addOption(Option.builder("topic")
            .desc("Create test topic")
            .required(false)
            .hasArgs()
            .build()
        );

        // Optionally enable SASL
        options.addOption(Option.builder("sasl")
            .desc("Enable SASL authentication")
            .required(false)
            .hasArg(false)
            .type(Boolean.class)
            .build()
        );

        // Optionally enable SSL
        options.addOption(Option.builder("ssl")
            .desc("Enable SSL")
            .required(false)
            .hasArg(false)
            .type(Boolean.class)
            .build()
        );

        try {
            final CommandLineParser parser = new DefaultParser();
            return parser.parse(options, args);
        } catch (final Exception exception) {
            System.out.println("ERROR: " + exception.getMessage() + "\n");
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "DevCluster", options);
            System.out.println("");
            System.out.flush();

            throw exception;
        }
    }
}