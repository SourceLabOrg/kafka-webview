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

package org.sourcelab.kafka.webview.ui.controller.api;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    /**
     * Test cannot load pages that require an admin role.
     */
    @Test
    @Transactional
    public void test_withoutAdminRole() throws Exception {
        testUrlWithOutAdminRole("/api/cluster/1/create/topic", true);
        testUrlWithOutAdminRole("/api/cluster/1/modify/topic", true);
        testUrlWithOutAdminRole("/api/cluster/1/consumer/remove", true);
    }

    /**
     * Test the create topic end point.
     */
    @Test
    @Transactional
    public void test_createTopic() throws Exception {
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
                .with(user(adminUserDetails))
                .with(csrf())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())

            // Validate submit button seems to show up.
            .andExpect(content().string(containsString("{\"operation\":\"CreateTopic\",\"result\":true,\"message\":\"\"}")));

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
                .with(user(adminUserDetails))
                .with(csrf())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
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
     * Test the list Consumers end point.
     */
    @Test
    @Transactional
    public void test_listConsumers() throws Exception {
        final String consumerId = "test-consumer-id-" + System.currentTimeMillis();

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster(
            "Test Cluster",
            sharedKafkaTestResource.getKafkaConnectString()
        );

        // Define our new topic name
        final String newTopic = "TestTopic-" + System.currentTimeMillis();
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(newTopic, 1, (short)1);

        // Publish records into topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, newTopic, 0);

        // Create a consumer and consume from the records, maintaining state.
        final Properties consumerProperties = new Properties();
        consumerProperties.put("client.id", consumerId);
        consumerProperties.put("group.id", consumerId);

        try (final KafkaConsumer consumer = sharedKafkaTestResource
            .getKafkaTestUtils()
            .getKafkaConsumer(StringDeserializer.class, StringDeserializer.class, consumerProperties)) {

            // Consume
            consumer.subscribe(Collections.singleton(newTopic));
            consumer.poll(2000L);

            // Save state.
            consumer.commitSync();
        }
        
        // Hit end point
        mockMvc
            .perform(get("/api/cluster/" + cluster.getId() + "/consumers")
                .with(user(nonAdminUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())

            // Validate submit button seems to show up.
            .andExpect(content().string(containsString("{\"id\":\"" + consumerId + "\",\"simple\":false}")));
    }
}