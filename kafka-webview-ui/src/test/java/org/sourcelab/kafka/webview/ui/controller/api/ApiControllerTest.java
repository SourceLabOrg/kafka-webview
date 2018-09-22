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
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

    /**
     * Test cannot load pages that require an admin role.
     */
    @Test
    @Transactional
    public void test_withoutAdminRole() throws Exception {
        testUrlWithOutAdminRole("/api/cluster/1/create/topic", true);
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
}