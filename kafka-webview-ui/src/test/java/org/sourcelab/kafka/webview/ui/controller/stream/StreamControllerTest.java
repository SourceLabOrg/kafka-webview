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

package org.sourcelab.kafka.webview.ui.controller.stream;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.controller.api.requests.ConsumeRequest;
import org.sourcelab.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StreamControllerTest extends AbstractMvcTest {
    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    @Autowired
    private ViewTestTools viewTestTools;

    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private ClusterTestTools clusterTestTools;

    @Autowired
    private StreamController streamController;

    @Autowired
    private WebSocketConsumersManager webSocketConsumersManager;

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        final View view = viewTestTools.createView("TestView");

        // View "stream" page.
        testUrlRequiresAuthentication("/stream/" + view.getId(), false);
    }

    /**
     * Smoke test stream page loads.  Does no real inspection that the page functions beyond
     * simply loading.
     */
    @Test
    @Transactional
    public void smokeTestStream() throws Exception {

        final View view = viewTestTools.createView("TestView");

        // Hit the stream page for specified view.
        mockMvc
            .perform(get("/stream/" + view.getId())
                .with(user(adminUserDetails)))
            //.andDo(print())
            .andExpect(status().isOk())
            // Contains some basic text
            .andExpect(content().string(containsString(view.getName())))
            .andExpect(content().string(containsString(view.getTopic())))
            .andExpect(content().string(containsString("Switch to View")))
            .andExpect(content().string(containsString("href=\"/view/" + view.getId() + "\"")));
    }

    /**
     * Test starting a new websocket consumer.
     */
    @Test
    public void shouldReceiveAMessageFromTheServer() throws Exception {
        final String expectedSessionId = "MYSESSIONID";
        final String topicName = "TestTopic" + System.currentTimeMillis();

        // Sanity test, no active consumers
        Assert.assertEquals("Should have no active consumers", 0, webSocketConsumersManager.countActiveConsumers());

        // Create a topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topicName, 2, (short) 1);

        // Create cluster
        final Cluster cluster = clusterTestTools
            .createCluster("TestCluster", sharedKafkaTestResource.getKafkaConnectString());

        // Create view
        final View view = viewTestTools
            .createViewWithCluster("TestView", cluster);

        // Sanity test, no enforced partitions
        assertEquals("Partitions Property should be empty string", "", view.getPartitions());

        final ConsumeRequest consumeRequest = new ConsumeRequest();
        consumeRequest.setAction("head");
        consumeRequest.setPartitions("0,1");
        consumeRequest.setFilters(new ArrayList<>());
        consumeRequest.setResultsPerPartition(10);

        final SimpMessageHeaderAccessor mockHeaderAccessor = mock(SimpMessageHeaderAccessor.class);
        final Authentication mockPrincipal = mock(Authentication.class);
        when(mockHeaderAccessor.getUser())
            .thenReturn(mockPrincipal);
        when(mockPrincipal.getPrincipal())
            .thenReturn(nonAdminUserDetails);
        when(mockHeaderAccessor.getSessionId())
            .thenReturn(expectedSessionId);

        try {
            final String result = streamController.newConsumer(
                view.getId(),
                consumeRequest,
                mockHeaderAccessor
            );
            assertEquals("Should return success response", "{success: true}", result);

            // Verify consumer stood up
            assertEquals("Should have one active consumer", 1, webSocketConsumersManager.countActiveConsumers());

            // Lets refresh the view entity and verify the partitions field is still empty.
            // Validates ISSUE-212
            final View updatedView = viewRepository.findById(view.getId()).get();
            assertEquals("Partitions Property should be empty string", "", updatedView.getPartitions());
        } finally {
            // Cleanup, disconnect websocket consumers
            webSocketConsumersManager.removeConsumersForSessionId(expectedSessionId);
        }
    }
}
