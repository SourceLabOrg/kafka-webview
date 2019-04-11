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

package org.sourcelab.kafka.webview.ui.controller.configuration.stream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.socket.StreamConsumerDetails;
import org.sourcelab.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StreamConfigControllerTest extends AbstractMvcTest {

    /**
     * Inject a mock WebSocketConsumersManager instance into our controller.
     */
    @MockBean
    WebSocketConsumersManager mockConsumerManager;

    @Autowired
    private ViewTestTools viewTestTools;

    @Autowired
    private UserTestTools userTestTools;

    @Autowired
    private ClusterTestTools clusterTestTools;

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        // Stream Consumer index page.
        testUrlRequiresAuthentication("/configuration/stream", false);

        // Stream Consumer delete page.
        testUrlRequiresAuthentication("/configuration/stream/close/SomeHashHere", true);
    }

    /**
     * Ensure correct permissions are required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthorization() throws Exception {
        // Stream Consumer index page.
        testUrlRequiresPermission("/configuration/stream", false, Permissions.CONSUMER_READ);

        // Stream Consumer delete page.
        testUrlRequiresPermission("/configuration/stream/close/SomeHashHere", true, Permissions.CONSUMER_DELETE);
    }

    /**
     * Test index page.
     */
    @Test
    @Transactional
    public void test_index() throws Exception {
        final Permissions[] permissions = {
            Permissions.CONSUMER_READ,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String viewName1 = "My first view";
        final String viewName2 = "My second view";

        final String sessionHash1 = "SessionHash1";
        final String sessionHash2 = "SessionHash2";

        final String userName1 = "First User Display Name";
        final String userName2 = "Second User Display Name";

        final String clusterName1 = "ClusterA";
        final String clusterName2 = "ClusterB";

        final long consumedRecordCount1 = 234243L;
        final long consumedRecordCount2 = 2334L;

        final long startedAtTimestamp1 = 1529841610L;
        final String startedAtDate1 = "";

        final long startedAtTimestamp2 = 977702112L;
        final String startedAtDate2 = "";

        // Create some view objects
        final View view1 = viewTestTools.createView(viewName1);
        final Cluster cluster1 = view1.getCluster();
        cluster1.setName(clusterName1);
        clusterTestTools.save(cluster1);

        final View view2 = viewTestTools.createView(viewName2);
        final Cluster cluster2 = view2.getCluster();
        cluster2.setName(clusterName2);
        clusterTestTools.save(cluster2);

        // Create some user objects
        final User user1 = userTestTools.createUser();
        user1.setDisplayName(userName1);
        userTestTools.save(user1);

        final User user2 = userTestTools.createUser();
        user2.setDisplayName(userName2);
        userTestTools.save(user2);

        // Define some mock entries
        final StreamConsumerDetails consumerDetails1 = new StreamConsumerDetails(
            user1.getId(), view1.getId(), sessionHash1, startedAtTimestamp1, consumedRecordCount1, false
        );

        final StreamConsumerDetails consumerDetails2 = new StreamConsumerDetails(
            user2.getId(), view2.getId(), sessionHash2, startedAtTimestamp2, consumedRecordCount2, true
        );

        final ArrayList<StreamConsumerDetails> consumerDetails = new ArrayList<>();
        consumerDetails.add(consumerDetails1);
        consumerDetails.add(consumerDetails2);

        // When our mock is asked for the consumers, return the above values.
        when(mockConsumerManager.getConsumers())
            .thenReturn(Collections.unmodifiableCollection(consumerDetails));

        // Hit index.
        mockMvc
            .perform(get("/configuration/stream").with(user(user)))
            .andExpect(status().isOk())
            // Validate consumer 1
            .andExpect(content().string(containsString(userName1)))

            // Link to view 1
            .andExpect(content().string(containsString(viewName1)))
            .andExpect(content().string(containsString("\"/view/" + view1.getId() + "\"")))
            // Link to cluster 1
            .andExpect(content().string(containsString(clusterName1)))
            .andExpect(content().string(containsString("\"/cluster/" + view1.getCluster().getId() + "\"")))
            // Other fields
            .andExpect(content().string(containsString(Long.toString(startedAtTimestamp1))))
            .andExpect(content().string(containsString(Long.toString(consumedRecordCount1))))
            .andExpect(content().string(containsString("\"/configuration/stream/close/" + sessionHash1 + "\"")))

            // Validate consumer 2
            .andExpect(content().string(containsString(userName2)))

            // Link to view 2
            .andExpect(content().string(containsString(viewName2)))
            .andExpect(content().string(containsString("\"/view/" + view2.getId() + "\"")))
            // Link to cluster 2
            .andExpect(content().string(containsString(clusterName2)))
            .andExpect(content().string(containsString("\"/cluster/" + view2.getCluster().getId() + "\"")))
            // Other fields
            .andExpect(content().string(containsString(Long.toString(startedAtTimestamp2))))
            .andExpect(content().string(containsString(Long.toString(consumedRecordCount2))))
            .andExpect(content().string(containsString("\"/configuration/stream/close/" + sessionHash2 + "\"")));
    }

    /**
     * Test closing a consumer with valid sessionHash.
     */
    @Test
    @Transactional
    public void test_closeValidHash() throws Exception {
        final Permissions[] permissions = {
            Permissions.CONSUMER_DELETE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String sessionHash = "SessionHash1";

        // When our mock is asked for the consumers, return the above values.
        when(mockConsumerManager.removeConsumersForSessionHash(eq(sessionHash)))
            .thenReturn(true);

        // Hit index.
        final MvcResult result = mockMvc
            .perform(post("/configuration/stream/close/" + sessionHash)
                .with(user(user))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/stream"))
            .andExpect(flash().attributeExists("FlashMessage"))
            .andReturn();

        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        Assert.assertNotNull(flashMessage);
        Assert.assertEquals("success", flashMessage.getType());

        // Verify mock was called
        Mockito.verify(mockConsumerManager, times(1))
            .removeConsumersForSessionHash(eq(sessionHash));
    }

    /**
     * Test closing a consumer with an invalid sessionHash.
     */
    @Test
    @Transactional
    public void test_closeInvalidHash() throws Exception {
        final Permissions[] permissions = {
            Permissions.CONSUMER_DELETE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String sessionHash = "SessionHash1";

        // When our mock is asked for the consumers, return the above values.
        when(mockConsumerManager.removeConsumersForSessionHash(eq(sessionHash)))
            .thenReturn(false);

        // Hit index.
        final MvcResult result = mockMvc
            .perform(post("/configuration/stream/close/" + sessionHash)
                .with(user(user))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/stream"))
            .andExpect(flash().attributeExists("FlashMessage"))
            .andReturn();

        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        Assert.assertNotNull(flashMessage);
        Assert.assertEquals("warning", flashMessage.getType());

        // Verify mock was called
        Mockito.verify(mockConsumerManager, times(1))
            .removeConsumersForSessionHash(eq(sessionHash));
    }
}