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

package org.sourcelab.kafka.webview.ui.controller.cluster;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
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

    /**
     * Content associated with the ability to view topics.
     */
    private static final String CONSUMERS_CONTENT_CLASS = "consumers-content";

    /**
     * Content associated with the ability to remove consumers.
     */
    private static final String CONSUMERS_REMOVE_CONTENT_CLASS = "remove-consumer-content";

    /**
     * Content associated with the ability to view topics.
     */
    private static final String TOPICS_CONTENT_CLASS = "topics-content";

    /**
     * Content associated with the ability to remove topics.
     */
    private static final String TOPICS_REMOVE_CONTENT_CLASS = "remove-topics-content";

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
     * Ensure correct permissions are required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthorization() throws Exception {
        // Create at least one cluster.
        final Cluster cluster = clusterTestTools.createCluster("Test Cluster " + System.currentTimeMillis());

        // Cluster index page.
        testUrlRequiresPermission("/cluster", false, Permissions.CLUSTER_READ);

        // Cluster read page.
        testUrlRequiresPermission("/cluster/" + cluster.getId(), false, Permissions.CLUSTER_READ);

        // Cluster broker read page.
        testUrlRequiresPermission("/cluster/" + cluster.getId() + "/broker/0", false, Permissions.CLUSTER_READ);

        // Topic Read requires both CLUSTER_READ, and TOPIC_READ perms.
        testUrlRequiresPermission(
            "/cluster/" + cluster.getId() + "/topic/test",
            false,
            Permissions.TOPIC_READ, Permissions.CLUSTER_READ
        );

        // Consumer Read requires both CLUSTER_READ and CONSUMER_READ perms.
        testUrlRequiresPermission(
            "/cluster/" + cluster.getId() + "/consumer/test",
            false,
            Permissions.CONSUMER_READ, Permissions.CLUSTER_READ
        );
    }

    /**
     * Test loading index page where:
     * - no clusters created,
     * - has CLUSTER_CREATE permission.
     *
     * Result is that you should see a message telling you no clusters exist, and a link to create one.
     */
    @Test
    @Transactional
    public void test_indexAsAdminWithNoClustersShowsCreateClusterLink() throws Exception {
        final Permissions[] permissions = {
            Permissions.CLUSTER_CREATE,
            Permissions.CLUSTER_READ,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit the index page.
        mockMvc
            .perform(get("/cluster/")
                .with(user(user)))
            .andExpect(status().isOk())
            // Should contain this text
            .andExpect(content().string(containsString(ClusterTestTools.NO_CLUSTERS_SETUP_TEXT)))
            .andExpect(content().string(containsString(ClusterTestTools.CREATE_CLUSTER_TEXT)))
            .andExpect(content().string(containsString(ClusterTestTools.CREATE_CLUSTER_LINK)))
            // But not this
            .andExpect(content().string(not(containsString(ClusterTestTools.ASK_ADMIN_CREATE_CLUSTER_TEXT))));
    }

    /**
     * Test loading index page where:
     * - no clusters created,
     * - has CLUSTER_CREATE permission.
     *
     * Result is that you should see a message telling you no clusters exist, and text telling you to
     * get an admin to create one.
     */
    @Test
    @Transactional
    public void test_indexAsNonAdminWithNoClustersShowsCreateText() throws Exception {
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit the index page.
        mockMvc
            .perform(get("/cluster/")
                .with(user(user)))
            //.andDo(print())
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
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster
        final String cluster1Name = "My Test Cluster";
        final String cluster2Name = "My Other Test Cluster";
        final Cluster cluster1 = clusterTestTools.createCluster(cluster1Name);
        final Cluster cluster2 = clusterTestTools.createCluster(cluster2Name);

        // Hit the index page.
        mockMvc
            .perform(get("/cluster/")
                .with(user(user)))
            .andExpect(status().isOk())
            // Should contain this text
            .andExpect(content().string(containsString("Kafka Clusters")))
            .andExpect(content().string(containsString(cluster1Name)))
            .andExpect(content().string(containsString(cluster2Name)))
            .andExpect(content().string(containsString("\"/cluster/" + cluster1.getId() + "\"")))
            .andExpect(content().string(containsString("\"/cluster/" + cluster2.getId() + "\"")))
            .andExpect(content().string(containsString("\"/view?clusterId=" + cluster1.getId() + "\"")))
            .andExpect(content().string(containsString("\"/view?clusterId=" + cluster2.getId() + "\"")))
            // But not this
            .andExpect(content().string(not(containsString(ClusterTestTools.NO_CLUSTERS_SETUP_TEXT))));
    }

    /**
     * Test loading read page with all permissions and you see everything.
     */
    @Test
    @Transactional
    public void test_readCluster_withAllPermissions() throws Exception {
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.TOPIC_READ,
            Permissions.TOPIC_CREATE,
            Permissions.TOPIC_DELETE,
            Permissions.CONSUMER_READ,
            Permissions.CONSUMER_DELETE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");

        // Hit the read page.
        mockMvc
            .perform(get("/cluster/" + cluster.getId())
                .with(user(user)))
            .andExpect(status().isOk())
            // Validate topics are displayed
            .andExpect(content().string(containsString(TOPICS_CONTENT_CLASS)))
            .andExpect(content().string(containsString(CREATE_TOPIC_LINK_TEXT)))
            .andExpect(content().string(containsString(TOPICS_REMOVE_CONTENT_CLASS)))
            // Validate consumers are displayed
            .andExpect(content().string(containsString(CONSUMERS_CONTENT_CLASS)))
            .andExpect(content().string(containsString(CONSUMERS_REMOVE_CONTENT_CLASS)));
    }

    /**
     * Test loading read page WITHOUT TOPIC_READ permission and no topic content is displayed.
     */
    @Test
    @Transactional
    public void test_readCluster_withOutTopicReadPermission() throws Exception {
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");

        // Hit the read page.
        mockMvc
            .perform(get("/cluster/" + cluster.getId())
                .with(user(user)))
            .andExpect(status().isOk())
            // Validate topics are NOT displayed
            .andExpect(content().string(not(containsString(TOPICS_CONTENT_CLASS))))
            .andExpect(content().string(not(containsString(CREATE_TOPIC_LINK_TEXT))))
            .andExpect(content().string(not(containsString(TOPICS_REMOVE_CONTENT_CLASS))));
    }

    /**
     * Test loading read page with TOPIC_CREATE permission shows 'create topic' link.
     */
    @Test
    @Transactional
    public void test_readClusterShowsCreateTopicLink_withPermission() throws Exception {
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.TOPIC_CREATE,
            Permissions.TOPIC_READ,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");

        // Hit the read page.
        mockMvc
            .perform(get("/cluster/" + cluster.getId())
                .with(user(user)))
            .andExpect(status().isOk())
            // Validate topics are displayed
            .andExpect(content().string(containsString(TOPICS_CONTENT_CLASS)))
            // Validate 'create topic' link exists
            .andExpect(content().string(containsString(CREATE_TOPIC_LINK_TEXT)))
            // But not remove topic.
            .andExpect(content().string(not(containsString(TOPICS_REMOVE_CONTENT_CLASS))));
    }

    /**
     * Test loading read page WITHOUT TOPIC_CREATE permission will NOT show 'create topic' link.
     */
    @Test
    @Transactional
    public void test_readClusterShowsCreateTopicLink_withoutPermission() throws Exception {
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.TOPIC_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");

        // Hit the read page.
        mockMvc
            .perform(get("/cluster/" + cluster.getId())
                .with(user(user)))
            .andExpect(status().isOk())
            // Validate topics are displayed
            .andExpect(content().string(containsString(TOPICS_CONTENT_CLASS)))
            // Validate 'create topic' link does NOT exist
            .andExpect(content().string(not(containsString(CREATE_TOPIC_LINK_TEXT))))
            // Validate no remove topic content.
            .andExpect(content().string(not(containsString(TOPICS_REMOVE_CONTENT_CLASS))));
    }

    /**
     * Test loading read page WITHOUT CONSUMER_READ permission and no consumer content is displayed.
     */
    @Test
    @Transactional
    public void test_readCluster_withOutConsumerReadPermission() throws Exception {
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.TOPIC_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");

        // Hit the read page.
        mockMvc
            .perform(get("/cluster/" + cluster.getId())
                .with(user(user)))
            .andExpect(status().isOk())
            // Validate topics are displayed
            .andExpect(content().string(containsString(TOPICS_CONTENT_CLASS)))
            // Validate no consumer details are displayed
            .andExpect(content().string(not(containsString(CONSUMERS_CONTENT_CLASS))))
            .andExpect(content().string(not(containsString(CONSUMERS_REMOVE_CONTENT_CLASS))));
    }

    /**
     * Test loading read page with CONSUMER_DELETE permission shows 'remove consumer' link.
     */
    @Test
    @Transactional
    public void test_readClusterShowsRemoveConsumer_withPermission() throws Exception {
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
            Permissions.CONSUMER_DELETE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");

        // Hit the read page.
        mockMvc
            .perform(get("/cluster/" + cluster.getId())
                .with(user(user)))
            .andExpect(status().isOk())
            // Validate 'remove consumers' content does exist on the page.
            .andExpect(content().string(containsString(CONSUMERS_REMOVE_CONTENT_CLASS)));
    }

    /**
     * Test loading read page with CONSUMER_DELETE permission shows 'remove consumer' link.
     */
    @Test
    @Transactional
    public void test_readClusterShowsRemoveConsumer_withOutPermission() throws Exception {
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a cluster.
        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");

        // Hit the read page.
        mockMvc
            .perform(get("/cluster/" + cluster.getId())
                .with(user(user)))
            .andExpect(status().isOk())
            // Validate 'remove consumers' content does NOT exist on the page.
            .andExpect(content().string(not(containsString(CONSUMERS_REMOVE_CONTENT_CLASS))));
    }
}