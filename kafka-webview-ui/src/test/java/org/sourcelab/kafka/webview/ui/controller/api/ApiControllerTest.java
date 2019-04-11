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

package org.sourcelab.kafka.webview.ui.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.controller.api.requests.ConsumeRequest;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApiControllerTest extends AbstractMvcTest {

    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    @Autowired
    private ClusterTestTools clusterTestTools;

    @Autowired
    private ViewTestTools viewTestTools;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        // Consume view endpoint.
        testUrlRequiresAuthentication("/api/consumer/view/1", true);

        // Update consumer offsets end point.
        testUrlRequiresAuthentication("/api/consumer/view/1/offsets", true);

        // Update consumer offsets end point using timestamp
        testUrlRequiresAuthentication("/api/consumer/view/1/timestamp/123123123213", true);

        // Get available partitions for a view
        testUrlRequiresAuthentication("/api/view/1/partitions" , false);

        // Get available topics on cluster
        testUrlRequiresAuthentication("/api/cluster/1/topics/list" , false);

        // Get details about a specific topic on cluster
        testUrlRequiresAuthentication("/api/cluster/1/topic/TopicNameHere/details" , false);

        // Get configuration about a specific topic on cluster.
        testUrlRequiresAuthentication("/api/cluster/1/topic/TopicNameHere/config" , false);

        // Get config for a specific broker on a cluster
        testUrlRequiresAuthentication("/api/cluster/1/broker/2/config" , false);

        // Get details about ALL topics on a cluster
        testUrlRequiresAuthentication("/api/cluster/1/topics/details" , false);

        // Create topic on a cluster
        testUrlRequiresAuthentication("/api/cluster/1/create/topic" , true);

        // Modify a topic on a cluster
        testUrlRequiresAuthentication("/api/cluster/1/modify/topic" , true);

        // Delete a topic on a cluster
        testUrlRequiresAuthentication("/api/cluster/1/modify/delete" , true);

        // Read nodes on a cluster
        testUrlRequiresAuthentication("/api/cluster/1/nodes" , false);

        // Get list of available filter options
        testUrlRequiresAuthentication("/api/filter/1/options" , false);

        // Get list of consumers in cluster
        testUrlRequiresAuthentication("/api/cluster/1/consumers" , false);

        // Get list of consumers in cluster with details
        testUrlRequiresAuthentication("/api/cluster/1/consumersAndDetails" , false);

        // Get details about a specific consumer
        testUrlRequiresAuthentication("/api/cluster/1/consumer/ConsumerGroupIdHere/details" , false);

        // Get offsets about a specific consumer
        testUrlRequiresAuthentication("/api/cluster/1/consumer/ConsumerGroupIdHere/offsets" , false);

        // Get offsets + topic tail offsets about a specific consumer
        testUrlRequiresAuthentication("/api/cluster/1/consumer/ConsumerGroupIdHere/offsetsAndTailPositions" , false);

        // Remove a consumer
        testUrlRequiresAuthentication("/api/cluster/234234/consumer/remove" , true);
    }

    /**
     * Ensure correct permissions are required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthorization() throws Exception {
        // Consume view endpoint.
        testUrlRequiresPermission("/api/consumer/view/1" , true, Permissions.VIEW_READ);

        // Update consumer offsets end point.
        testUrlRequiresPermission("/api/consumer/view/1/offsets" , true, Permissions.VIEW_READ);

        // Update consumer offsets end point using timestamp
        testUrlRequiresPermission("/api/consumer/view/1/timestamp/123123123213" , true, Permissions.VIEW_READ);

        // Get available partitions for a view
        testUrlRequiresPermission("/api/view/1/partitions" , false, Permissions.VIEW_READ);

        // Get available topics on cluster
        testUrlRequiresPermission("/api/cluster/1/topics/list" , false, Permissions.TOPIC_READ, Permissions.CLUSTER_READ);

        // Get details about a specific topic on cluster
        testUrlRequiresPermission("/api/cluster/1/topic/TopicNameHere/details" , false, Permissions.TOPIC_READ, Permissions.CLUSTER_READ);

        // Get configuration about a specific topic on cluster.
        testUrlRequiresPermission("/api/cluster/1/topic/TopicNameHere/config" , false, Permissions.TOPIC_READ, Permissions.CLUSTER_READ);

        // Get config for a specific broker on a cluster
        testUrlRequiresPermission("/api/cluster/1/broker/2/config" , false, Permissions.CLUSTER_READ);

        // Get details about ALL topics on a cluster
        testUrlRequiresPermission("/api/cluster/1/topics/details" , false, Permissions.TOPIC_READ, Permissions.CLUSTER_READ);

        // Create topic on a cluster
        testUrlRequiresPermission("/api/cluster/1/create/topic" , true, Permissions.TOPIC_CREATE);

        // Modify a topic on a cluster
        testUrlRequiresPermission("/api/cluster/1/modify/topic" , true, Permissions.TOPIC_MODIFY);

        // Delete a topic on a cluster
        testUrlRequiresPermission("/api/cluster/1/delete/topic" , true, Permissions.TOPIC_DELETE);

        // Read nodes on a cluster
        testUrlRequiresPermission("/api/cluster/1/nodes" , false, Permissions.CLUSTER_READ);

        // Get list of available filter options
        testUrlRequiresPermission("/api/filter/1/options" , false, Permissions.VIEW_READ);

        // Get list of consumers in cluster
        testUrlRequiresPermission("/api/cluster/1/consumers" , false, Permissions.CLUSTER_READ, Permissions.CONSUMER_READ);

        // Get list of consumers in cluster with details
        testUrlRequiresPermission("/api/cluster/1/consumersAndDetails" , false, Permissions.CLUSTER_READ, Permissions.CONSUMER_READ);

        // Get details about a specific consumer
        testUrlRequiresPermission("/api/cluster/1/consumer/ConsumerGroupIdHere/details" , false, Permissions.CLUSTER_READ, Permissions.CONSUMER_READ);

        // Get offsets about a specific consumer
        testUrlRequiresPermission("/api/cluster/1/consumer/ConsumerGroupIdHere/offsets" , false, Permissions.CLUSTER_READ, Permissions.CONSUMER_READ);

        // Get offsets + topic tail offsets about a specific consumer
        testUrlRequiresPermission("/api/cluster/1/consumer/ConsumerGroupIdHere/offsetsAndTailPositions" , false, Permissions.CLUSTER_READ, Permissions.CONSUMER_READ);

        // Remove a consumer
        testUrlRequiresPermission("/api/cluster/234234/consumer/remove" , true, Permissions.CONSUMER_DELETE);

    }

    /**
     * Attempts to consume a view.
     *
     * Verifies that HEAD, NEXT, and PREV works as expected.
     */
    @Test
    @Transactional
    public void test_consumeView() throws Exception {
        // Create user with VIEW_READ permission
        final Permissions[] permissions = {
            Permissions.VIEW_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String topicName = "MyNewTopic-" + System.currentTimeMillis();

        // Create a topic in cluster with 2 partitions.
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topicName, 2, (short) 1);

        // Publish data into both partitions.
        for (int partitionId = 0; partitionId < 2; partitionId++) {
            // Generate data to publish into partition
            // Use a LinkedHashMap to keep ordering.
            final Map<byte[], byte[]> dataToPublish = new LinkedHashMap<>();
            for (int entry = 0; entry < 100; entry++) {
                dataToPublish.put(
                    ("Key Partition:" + partitionId + " Entry:" + entry).getBytes(Charset.forName("utf-8")),
                    ("Data Partition:" + partitionId + " Entry:" + entry).getBytes(Charset.forName("utf-8"))
                );
            }

            // Produce em.
            sharedKafkaTestResource
                .getKafkaTestUtils()
                .produceRecords(dataToPublish, topicName, partitionId);
        }

        // Create cluster
        final Cluster cluster = clusterTestTools.createCluster("MyTestCluster", sharedKafkaTestResource.getKafkaConnectString());

        // Create View
        final View view = viewTestTools.createViewWithCluster("MyTestView", cluster);
        view.setTopic(topicName);
        viewTestTools.save(view);

        final int resultsPerPartition = 10;

        // Construct payload with starting at head.
        final ConsumeRequest consumeRequest = new ConsumeRequest();
        consumeRequest.setAction("head");
        consumeRequest.setPartitions("");
        consumeRequest.setResultsPerPartition(resultsPerPartition);
        String payload = objectMapper.writeValueAsString(consumeRequest);

        // Make request to API to consume from head.
        ResultActions resultActions = mockMvc
            .perform(post("/api/consumer/view/" + view.getId())
                .with(withAuthentication(user))
                .with(csrf())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk());

        // Verify we found each key and value for first 10 entries on each partition.
        for (int partitionId = 0; partitionId < 2; partitionId++) {
            for (int entry = 0; entry < resultsPerPartition; entry++) {
                final String expectedEntryHeader = "\"partition\":" + partitionId + ",\"offset\":" + entry + ",";
                final String expectedEntryKey = "\"key\":\"Key Partition:" + partitionId + " Entry:" + entry + "\"";
                final String expectedEntryData = "\"value\":\"Data Partition:" + partitionId + " Entry:" + entry;

                resultActions
                    .andExpect(content().string(containsString(expectedEntryHeader)))
                    .andExpect(content().string(containsString(expectedEntryKey)))
                    .andExpect(content().string(containsString(expectedEntryData)));
            }
        }

        // Now request "next"
        consumeRequest.setAction("next");
        payload = objectMapper.writeValueAsString(consumeRequest);

        resultActions = mockMvc
            .perform(post("/api/consumer/view/" + view.getId())
                .with(withAuthentication(user))
                .with(csrf())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // Validate we get the next entries

        // Verify we found each key and value for 2nd 10 entries on each partition.
        for (int partitionId = 0; partitionId < 2; partitionId++) {
            for (int entry = resultsPerPartition; entry < (resultsPerPartition * 2); entry++) {
                final String expectedEntryHeader = "\"partition\":" + partitionId + ",\"offset\":" + entry + ",";
                final String expectedEntryKey = "\"key\":\"Key Partition:" + partitionId + " Entry:" + entry + "\"";
                final String expectedEntryData = "\"value\":\"Data Partition:" + partitionId + " Entry:" + entry;

                resultActions
                    .andExpect(content().string(containsString(expectedEntryHeader)))
                    .andExpect(content().string(containsString(expectedEntryKey)))
                    .andExpect(content().string(containsString(expectedEntryData)));
            }
        }

        // Now request "prev"
        consumeRequest.setAction("previous");
        payload = objectMapper.writeValueAsString(consumeRequest);

        resultActions = mockMvc
            .perform(post("/api/consumer/view/" + view.getId())
                .with(withAuthentication(user))
                .with(csrf())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // Validate we get the previous 10 entries

        // Verify we found each key and value for first 10 entries on each partition.
        for (int partitionId = 0; partitionId < 2; partitionId++) {
            for (int entry = 0; entry < resultsPerPartition; entry++) {
                final String expectedEntryHeader = "\"partition\":" + partitionId + ",\"offset\":" + entry + ",";
                final String expectedEntryKey = "\"key\":\"Key Partition:" + partitionId + " Entry:" + entry + "\"";
                final String expectedEntryData = "\"value\":\"Data Partition:" + partitionId + " Entry:" + entry;

                resultActions
                    .andExpect(content().string(containsString(expectedEntryHeader)))
                    .andExpect(content().string(containsString(expectedEntryKey)))
                    .andExpect(content().string(containsString(expectedEntryData)));
            }
        }
    }

    /**
     * Test the create topic end point.
     */
    @Test
    @Transactional
    public void test_createTopic() throws Exception {
        // Create user with TOPIC_CREATE permission
        final Permissions[] permissions = {
            Permissions.TOPIC_CREATE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Define our new topic name
        final String newTopic = "TestTopic-" + System.currentTimeMillis();

        // Sanity test, verify topic doesn't exists
        Set<String> topicNames = sharedKafkaTestResource
            .getKafkaTestUtils()
            .getTopicNames();

        // sanity test.
        assertFalse("Topic should not exist yet", topicNames.contains(newTopic));

        // Construct payload
        final String payload = "{ \"name\": \"" + newTopic + "\", \"partitions\": 1, \"replicas\": 1}";

        // Hit end point as admin user
        mockMvc
            .perform(post("/api/cluster/" + cluster.getId() + "/create/topic")
                .with(user(user))
                .with(csrf())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
            )
            //.andDo(print())
            .andExpect(status().isOk())

            // Validate submit button seems to show up.
            .andExpect(content().string(containsString(
                "{\"operation\":\"CreateTopic\",\"result\":true,\"message\":\"Created topic '" + newTopic + "'\"}"
            )));

        // Validate topic now exists
        // Sanity test, verify topic doesn't exists
        topicNames = sharedKafkaTestResource
            .getKafkaTestUtils()
            .getTopicNames();

        // sanity test.
        assertTrue("Topic should exist now", topicNames.contains(newTopic));
    }

    /**
     * Test the modify topic configuration end point.
     */
    @Test
    @Transactional
    public void test_modifyTopic() throws Exception {
        // Create user with TOPIC_MODIFY permission
        final Permissions[] permissions = {
            Permissions.TOPIC_MODIFY
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Define our new topic name
        final String topicName = "TestTopic-" + System.currentTimeMillis();

        // Define the values we want to modify
        final String configName1 = "flush.messages";
        final String newConfigValue1 = "0";

        final String configName2 = "max.message.bytes";
        final String newConfigValue2 = "1024";


        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create a new topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topicName, 1, (short) 1);

        // Construct payload
        final String payload = "{ \"topic\": \"" + topicName + "\", \"config\": {" +
            "\"" + configName1 + "\": \"" + newConfigValue1 + "\", " +
            "\"" + configName2 + "\": \"" + newConfigValue2 + "\"" +
            "}}";

        // Hit end point as admin user
        final MvcResult result = mockMvc
            .perform(post("/api/cluster/" + cluster.getId() + "/modify/topic")
                .with(user(user))
                .with(csrf())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
            )
            //.andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        // Grab out the response
        final String resultJsonStr = result.getResponse().getContentAsString();

        // Half assed validation
        final String targetItem1 = "{\"name\":\"" + configName1 + "\",\"value\":\"" + newConfigValue1 + "\",\"default\":false}";
        final String targetItem2 = "{\"name\":\"" + configName2 + "\",\"value\":\"" + newConfigValue2 + "\",\"default\":false}";

        assertTrue(resultJsonStr.contains(targetItem1));
        assertTrue(resultJsonStr.contains(targetItem2));
    }

    /**
     * Test the delete topic end point.
     */
    @Test
    @Transactional
    public void test_deleteTopic() throws Exception {
        // Create user with TOPIC_DELETE permission
        final Permissions[] permissions = {
            Permissions.TOPIC_DELETE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Define our new topic name
        final String topicName = "DeleteTestTopic-" + System.currentTimeMillis();

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create a new topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topicName, 1, (short) 1);

        // Sanity test - Validate topic exists
        Set<String> topicNames = sharedKafkaTestResource
            .getKafkaTestUtils()
            .getTopicNames();
        assertTrue("Topic should exist now", topicNames.contains(topicName));

        // Construct payload
        final String payload = "{ \"name\": \"" + topicName + "\" }";

        // Hit end point as admin user
        mockMvc
            .perform(post("/api/cluster/" + cluster.getId() + "/delete/topic")
                .with(user(user))
                .with(csrf())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())

            // Validate result message seems correct.
            .andExpect(content().string(containsString("{\"operation\":\"DeleteTopic\",\"result\":true,\"message\":\"Removed topic '" + topicName + "'\"}")));

        // Validate topic no longer exists
        topicNames = sharedKafkaTestResource
            .getKafkaTestUtils()
            .getTopicNames();
        assertFalse("Topic should not exist now", topicNames.contains(topicName));
    }

    /**
     * Test the list Consumers end point.
     */
    @Test
    @Transactional
    public void test_listConsumers() throws Exception {
        // Create user with permissions
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.CONSUMER_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create a consumer with state on the cluster.
        final String consumerId = createConsumerWithState();

        // Hit end point
        mockMvc
            .perform(get("/api/cluster/" + cluster.getId() + "/consumers")
                .with(user(user))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
            )
            //.andDo(print())
            .andExpect(status().isOk())

            // Validate submit button seems to show up.
            .andExpect(content().string(containsString("{\"id\":\"" + consumerId + "\",\"simple\":false}")));
    }

    /**
     * Test the remove Consumer end point with admin role.
     */
    @Test
    @Transactional
    public void test_removeConsumer_withAdminRole() throws Exception {
        // Create user with permissions
        final Permissions[] permissions = {
            Permissions.CONSUMER_DELETE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create a consumer with state on the cluster.
        final String consumerId = createConsumerWithState();

        // Construct payload
        final String payload = "{ \"consumerId\": \"" + consumerId + "\", \"clusterId\": \"" + cluster.getId() + "\"}";

        // Hit end point
        mockMvc
            .perform(post("/api/cluster/" + cluster.getId() + "/consumer/remove")
                .with(user(user))
                .with(csrf())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
            )
            //.andDo(print())
            .andExpect(status().isOk())

            // Validate submit button seems to show up.
            .andExpect(content().string(containsString("true")));

        // Verify consumer no longer exists
        try (final AdminClient adminClient = sharedKafkaTestResource
            .getKafkaTestUtils()
            .getAdminClient()) {

            final ListConsumerGroupsResult request = adminClient.listConsumerGroups();
            final Collection<ConsumerGroupListing> results = request.all().get();

            final Optional<ConsumerGroupListing> match = results.stream()
                .filter((entry) -> (entry.groupId().equals(consumerId)))
                .findFirst();

            assertFalse("Should not have found entry", match.isPresent());
        }
    }

    /**
     * Test the remove Consumer end point with non admin role.
     */
    @Test
    @Transactional
    public void test_removeConsumer_withNonAdminRole() throws Exception {
        // Create user with permissions
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.CONSUMER_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create a consumer with state on the cluster.
        final String consumerId = createConsumerWithState();

        // Construct payload
        final String payload = "{ \"consumerId\": \"" + consumerId + "\", \"clusterId\": \"" + cluster.getId() + "\"}";

        // Hit end point
        mockMvc
            .perform(post("/api/cluster/" + cluster.getId() + "/consumer/remove")
                .with(user(user))
                .with(csrf())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
            )
            //.andDo(print())
            .andExpect(status().isForbidden());

        // Verify consumer still exists
        try (final AdminClient adminClient = sharedKafkaTestResource
            .getKafkaTestUtils()
            .getAdminClient()) {

            final ListConsumerGroupsResult request = adminClient.listConsumerGroups();
            final Collection<ConsumerGroupListing> results = request.all().get();

            final Optional<ConsumerGroupListing> match = results.stream()
                .filter((entry) -> (entry.groupId().equals(consumerId)))
                .findFirst();

            assertTrue("Should have found entry", match.isPresent());
        }
    }

    /**
     * Test the list Consumers end point.
     */
    @Test
    @Transactional
    public void test_listConsumersAndDetails() throws Exception {
        // Create user with permissions
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.CONSUMER_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create a consumer with state on the cluster.
        final String consumerId = createConsumerWithState();

        // Hit end point
        mockMvc
            .perform(get("/api/cluster/" + cluster.getId() + "/consumersAndDetails")
                .with(user(user))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
            )
            //.andDo(print())
            .andExpect(status().isOk())

            // Should have content similar to:
            // {"consumerId":"test-consumer-id-1543825835154","partitionAssignor":"","state":"Empty","members":[],"coordinator":{"id":1,"host":"127.0.0.1","port":52168,"rack":null},"simple":false}]

            // Validate submit button seems to show up.
            .andExpect(content().string(containsString("{\"consumerId\":\"" + consumerId )))
            .andExpect(content().string(containsString("partitionAssignor")))
            .andExpect(content().string(containsString("state")))
            .andExpect(content().string(containsString("members")))
            .andExpect(content().string(containsString("coordinator")))
            .andExpect(content().string(containsString("\"simple\":false")));
    }

    /**
     * Test the get specific consumer end point.
     */
    @Test
    @Transactional
    public void test_specificConsumerDetails() throws Exception {
        // Create user with permissions
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.CONSUMER_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create a consumer with state on the cluster.
        final String consumerId = createConsumerWithState();

        // Hit end point
        mockMvc
            .perform(get("/api/cluster/" + cluster.getId() + "/consumer/" + consumerId + "/details")
                .with(user(user))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
            )
            //.andDo(print())
            .andExpect(status().isOk())

            // Should have content similar to:
            // {"consumerId":"test-consumer-id-1543909384618","partitionAssignor":"","state":"Empty","members":[],"coordinator":{"id":1,"host":"127.0.0.1","port":51229,"rack":null},"simple":false}

            // Validate submit button seems to show up.
            .andExpect(content().string(containsString("{\"consumerId\":\"" + consumerId )))
            .andExpect(content().string(containsString("partitionAssignor")))
            .andExpect(content().string(containsString("state")))
            .andExpect(content().string(containsString("members")))
            .andExpect(content().string(containsString("coordinator")))
            .andExpect(content().string(containsString("\"simple\":false")));
    }

    /**
     * Test the get specific consumer offsets.
     */
    @Test
    @Transactional
    public void test_specificConsumerOffsets() throws Exception {
        // Create user with permissions
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.CONSUMER_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create a consumer with state on the cluster.
        final String consumerId = createConsumerWithState();

        // Hit end point
        mockMvc
            .perform(get("/api/cluster/" + cluster.getId() + "/consumer/" + consumerId + "/offsets")
                .with(user(user))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
            )
            //.andDo(print())
            .andExpect(status().isOk())

            // Should have content similar to:
            // {"consumerId":"test-consumer-id-1543909610144","topic":"TestTopic-1543909610145","offsets":[{"partition":0,"offset":10}],"partitions":[0]}

            // Validate results seem right.
            .andExpect(content().string(containsString("\"consumerId\":\"" + consumerId )))
            .andExpect(content().string(containsString("\"topic\":\"TestTopic-")))
            .andExpect(content().string(containsString("\"offsets\":[{\"partition\":0,\"offset\":10}]")))
            .andExpect(content().string(containsString("\"partitions\":[0]")));
    }

    /**
     * Test the get specific consumer offsets with tail offsets.
     */
    @Test
    @Transactional
    public void test_specificConsumerOffsetsWithTailOffsets() throws Exception {
        // Create user with permissions
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.CONSUMER_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create a consumer with state on the cluster.
        final String consumerId = createConsumerWithState();

        // Hit end point
        mockMvc
            .perform(get("/api/cluster/" + cluster.getId() + "/consumer/" + consumerId + "/offsetsAndTailPositions")
                .with(user(user))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
            )
            //.andDo(print())
            .andExpect(status().isOk())

            // Should have content similar to:
            // {"consumerId":"test-consumer-id-1544775318028","topic":"TestTopic-1544775318028","offsets":[{"partition":0,"offset":10,"tail":10}],"partitions":[0]}

            // Validate results seem right.
            .andExpect(content().string(containsString("\"consumerId\":\"" + consumerId )))
            .andExpect(content().string(containsString("\"topic\":\"TestTopic-")))
            .andExpect(content().string(containsString("\"offsets\":[{\"partition\":0,\"offset\":10,\"tail\":10}]")))
            .andExpect(content().string(containsString("\"partitions\":[0]")));
    }

    /**
     * Test listing topics without a search string.
     */
    @Test
    @Transactional
    public void test_listTopics_noSearchString() throws Exception {
        // Create user with permissions
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.TOPIC_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Create some topics
        final String expectedTopic1 = "A-ExpectedTopic1-" + System.currentTimeMillis();
        final String expectedTopic2= "C-ExpectedTopic2-" + System.currentTimeMillis();
        final String expectedTopic3 = "B-ExpectedTopic3-" + System.currentTimeMillis();

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(expectedTopic1, 1, (short) 1);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(expectedTopic2, 1, (short) 1);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(expectedTopic3, 1, (short) 1);

        // Hit end point
        mockMvc
            .perform(get("/api/cluster/" + cluster.getId() + "/topics/list")
                .with(user(user))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())

            // Validate all topics show up.
            .andExpect(content().string(containsString(expectedTopic1)))
            .andExpect(content().string(containsString(expectedTopic2)))
            .andExpect(content().string(containsString(expectedTopic3)));
    }

    /**
     * Test listing topics with a search string only returns filtered results.
     */
    @Test
    @Transactional
    public void test_listTopics_withSearchString() throws Exception {
        // Create user with permissions
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.TOPIC_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        final String searchStr = "CaT";

        // Create some topics
        final String expectedTopic1 = "ExpectedTopic1-" + System.currentTimeMillis();
        final String expectedTopic2= "ExpectedCATTopic2-" + System.currentTimeMillis();
        final String expectedTopic3 = "ExpectedTopic3-" + System.currentTimeMillis();

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(expectedTopic1, 1, (short) 1);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(expectedTopic2, 1, (short) 1);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(expectedTopic3, 1, (short) 1);

        // Hit end point
        mockMvc
            .perform(get("/api/cluster/" + cluster.getId() + "/topics/list")
                .param("search", searchStr)
                .with(user(user))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())

            // Validate Only the filtered instance shows up.
            .andExpect(content().string(containsString(expectedTopic2)))
            .andExpect(content().string(Matchers.not(containsString(expectedTopic1))))
            .andExpect(content().string(Matchers.not(containsString(expectedTopic3))));
    }

    /**
     * Helper method to create a consumer with state on the given cluster.
     * @return Consumer group id created.
     */
    private String createConsumerWithState() {
        final int totalRecords = 10;
        final String consumerId = "test-consumer-id-" + System.currentTimeMillis();

        // Define our new topic name
        final String newTopic = "TestTopic-" + System.currentTimeMillis();
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(newTopic, 1, (short)1);

        // Publish records into topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(totalRecords, newTopic, 0);

        // Create a consumer and consume from the records, maintaining state.
        final Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerId);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerId);

        try (final KafkaConsumer consumer = sharedKafkaTestResource
            .getKafkaTestUtils()
            .getKafkaConsumer(StringDeserializer.class, StringDeserializer.class, consumerProperties)) {

            // Consume
            consumer.subscribe(Collections.singleton(newTopic));
            consumer.poll(Duration.ofSeconds(5));

            // Save state.
            consumer.commitSync();
        }

        return consumerId;
    }

    private RequestPostProcessor withAuthentication(final UserDetails userDetails) {
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
            userDetails,
            userDetails.getPassword(),
            userDetails.getAuthorities()
        );

        final WebAuthenticationDetails details = mock(WebAuthenticationDetails.class);
        when(details.getSessionId()).thenReturn("SessionId1");
        token.setDetails(details);

        return authentication(token);
    }
}