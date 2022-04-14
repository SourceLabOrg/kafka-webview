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

package org.sourcelab.kafka.webview.ui.controller.cluster;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ClusterControllerTest extends AbstractMvcTest {

    /**
     * The text on the create new topic link.
     */
    private static final String CREATE_TOPIC_LINK_TEXT = "&nbsp;Create new";

    @Autowired
    private ClusterTestTools clusterTestTools;

    /**
     * Clear out all clusters prior to running test.
     */
    @Before
    public void setup() {
        // Ensure all clusters have been removed.
        clusterTestTools.deleteAllClusters();
    }

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        // cluster index page.
        testUrlRequiresAuthentication("/cluster", false);
    }

    /**
     * Test loading index page with no clusters created, as an admin, you should see a message
     * telling you no clusters exist, and a link to create one.
     */
    @Test
    @Transactional
    public void test_indexAsAdminWithNoClustersShowsCreateClusterLink() throws Exception {
        // Hit the index page.
        mockMvc
            .perform(get("/cluster/")
                .with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Should contain this text
            .andExpect(content().string(containsString(ClusterTestTools.NO_CLUSTERS_SETUP_TEXT)))
            .andExpect(content().string(containsString(ClusterTestTools.CREATE_CLUSTER_TEXT)))
            .andExpect(content().string(containsString(ClusterTestTools.CREATE_CLUSTER_LINK)))
            // But not this
            .andExpect(content().string(not(containsString(ClusterTestTools.ASK_ADMIN_CREATE_CLUSTER_TEXT))));
    }

    /**
     * Test loading index page with no clusters created, as normal user, you should see a message
     * telling you no clusters exist, and text telling you to get an admin to create one.
     */
    @Test
    @Transactional
    public void test_indexAsNonAdminWithNoClustersShowsCreateText() throws Exception {
        // Hit the index page.
        mockMvc
            .perform(get("/cluster/")
                .with(user(nonAdminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Validate no clusters exists text
            .andExpect(content().string(containsString(ClusterTestTools.NO_CLUSTERS_SETUP_TEXT)))
            .andExpect(content().string(containsString(ClusterTestTools.ASK_ADMIN_CREATE_CLUSTER_TEXT)))
            // Shouldn't have these links
            .andExpect(content().string(not(containsString(ClusterTestTools.CREATE_CLUSTER_TEXT))))
            .andExpect(content().string(not(containsString(ClusterTestTools.CREATE_CLUSTER_LINK))));
    }

    /**
     * Test loading index page with a cluster created.
     */
    @Test
    @Transactional
    public void test_indexWithClusterShowsData() throws Exception {
        // Create a cluster
        final String cluster1Name = "My Test Cluster";
        final String cluster2Name = "My Other Test Cluster";
        final Cluster cluster1 = clusterTestTools.createCluster(cluster1Name);
        final Cluster cluster2 = clusterTestTools.createCluster(cluster2Name);

        // Hit the index page.
        mockMvc
            .perform(get("/cluster/")
                .with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Should contain this text
            .andExpect(content().string(containsString("Kafka Clusters")))
            .andExpect(content().string(containsString(cluster1Name)))
            .andExpect(content().string(containsString(cluster2Name)))
            .andExpect(content().string(containsString("\"/cluster/" + cluster1.getId() + "\"")))
            .andExpect(content().string(containsString("\"/cluster/" + cluster2.getId() + "\"")))
            .andExpect(content().string(containsString("\"/view?cluster.id=" + cluster1.getId() + "\"")))
            .andExpect(content().string(containsString("\"/view?cluster.id=" + cluster2.getId() + "\"")))
            // But not this
            .andExpect(content().string(not(containsString(ClusterTestTools.NO_CLUSTERS_SETUP_TEXT))));
    }

    /**
     * Test loading read page as admin shows 'create topic' link.
     */
    @Test
    @Transactional
    public void test_readIndexShowsCreateTopicLink_withAdminRole() throws Exception {
        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");

        // Hit the read page.
        mockMvc
            .perform(get("/cluster/" + cluster.getId())
                .with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Validate 'create topic' link exists
            .andExpect(content().string(containsString(CREATE_TOPIC_LINK_TEXT)));
    }

    /**
     * Test loading read page as non-admin will not show the 'create topic' link.
     */
    @Test
    @Transactional
    public void test_readIndexShowsCreateTopicLink_withoutAdminRole() throws Exception {
        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");

        // Hit the read page.
        mockMvc
            .perform(get("/cluster/" + cluster.getId())
                .with(user(nonAdminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Validate 'create topic' link exists
            .andExpect(content().string(not(containsString(CREATE_TOPIC_LINK_TEXT))));
    }
}