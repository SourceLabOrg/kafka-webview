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

package org.sourcelab.kafka.webview.ui.controller.view;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ViewControllerTest extends AbstractMvcTest {

    @Autowired
    private ClusterTestTools clusterTestTools;

    @Autowired
    private ViewTestTools viewTestTools;

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        final View view = viewTestTools.createView("TestView");

        // View index page.
        testUrlRequiresAuthentication("/view", false);

        // View "browse" page.
        testUrlRequiresAuthentication("/view/" + view.getId(), false);
    }

    /**
     * Ensure correct permissions are required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthorization() throws Exception {
        final View view = viewTestTools.createView("TestView");

        // View index page.
        testUrlRequiresPermission("/view", false, Permissions.VIEW_READ);

        // View "browse" page.
        testUrlRequiresPermission("/view/" + view.getId(), false, Permissions.VIEW_READ);
    }

    /**
     * Test loading index page where:
     *  - No clusters have been created yet.
     *  - No views have been created yet.
     *  - User DOES have the CLUSTER_CREATE permission.
     *
     * Result show show that no clusters exist, and prompt with a link to create a cluster.
     */
    @Test
    @Transactional
    public void test_indexAsAdminWithNoClustersShowsCreateClusterLink() throws Exception {
        // Users permissions
        final Permissions[] permissions = {
            Permissions.VIEW_READ,
            Permissions.CLUSTER_CREATE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit the index page.
        mockMvc
            .perform(get("/view/")
                .with(user(user)))
            //.andDo(print())
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
     *  - No clusters have been created yet.
     *  - No views have been created yet.
     *  - User does NOT have the CLUSTER_CREATE permission.
     *
     * Result show show that no clusters exist, and prompt with asking the user to ask someone else to create a cluster.
     */
    @Test
    @Transactional
    public void test_indexAsNonAdminWithNoClustersShowsCreateText() throws Exception {
        // Users permissions
        final Permissions[] permissions = {
            Permissions.VIEW_READ,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit the index page.
        mockMvc
            .perform(get("/view/")
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
     * Test loading index page where:
     *  - At least one cluster exists.
     *  - No views have been created yet.
     *  - User has the VIEW_CREATE permission.
     *
     * Result show show that no views exist, and prompt with a link to create a view.
     */
    @Test
    @Transactional
    public void test_indexAsAdminWithNoViewsShowsCreateClusterLink() throws Exception {
        // Create a cluster
        clusterTestTools.createCluster("My Cluster");

        // Users permissions
        final Permissions[] permissions = {
            Permissions.VIEW_READ,
            Permissions.VIEW_CREATE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit the index page.
        mockMvc
            .perform(get("/view/")
                .with(user(user)))
            //.andDo(print())
            .andExpect(status().isOk())
            // Should contain this text
            .andExpect(content().string(containsString(ViewTestTools.NO_VIEWS_SETUP_TEXT)))
            .andExpect(content().string(containsString(ViewTestTools.CREATE_VIEW_TEXT)))
            .andExpect(content().string(containsString(ViewTestTools.CREATE_VIEW_LINK)))
            // But not this
            .andExpect(content().string(not(containsString(ViewTestTools.ASK_ADMIN_CREATE_VIEW_TEXT))))
            .andExpect(content().string(not(containsString(ClusterTestTools.NO_CLUSTERS_SETUP_TEXT))));
    }

    /**
     * Test loading index page where:
     *  - A cluster has been created.
     *  - No views have been created yet.
     *  - User does NOT have the VIEW_CREATE permission.
     *
     * Result show show that no views exist, and prompt to ask them to ask someone to create a view.
     */
    @Test
    @Transactional
    public void test_indexAsNonAdminWithNoViewsShowsCreateClusterLink() throws Exception {
        // Create a cluster
        clusterTestTools.createCluster("My Cluster");

        // Users permissions
        final Permissions[] permissions = {
            Permissions.VIEW_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit the index page.
        mockMvc
            .perform(get("/view/")
                .with(user(user)))
            //.andDo(print())
            .andExpect(status().isOk())
            // Should contain this text
            .andExpect(content().string(containsString(ViewTestTools.NO_VIEWS_SETUP_TEXT)))
            .andExpect(content().string(containsString(ViewTestTools.ASK_ADMIN_CREATE_VIEW_TEXT)))

            // But not this
            .andExpect(content().string(not(containsString(ViewTestTools.CREATE_VIEW_TEXT))))
            .andExpect(content().string(not(containsString(ViewTestTools.CREATE_VIEW_LINK))))
            .andExpect(content().string(not(containsString(ClusterTestTools.NO_CLUSTERS_SETUP_TEXT))));
    }

    /**
     * Test loading index page with 2 clusters, and 2 views.
     */
    @Test
    @Transactional
    public void test_indexWithViews() throws Exception {
        final String cluster1Name = "MyCluster 1";
        final String cluster2Name = "MyCluster 2";

        final String view1Name = "Cluster1 View 1";
        final String view2Name = "Cluster2 View 1";

        final String view1Topic = "View1Topic";
        final String view2Topic = "View2Topic";

        // Create a cluster
        final Cluster cluster1 = clusterTestTools.createCluster(cluster1Name);
        final Cluster cluster2 = clusterTestTools.createCluster(cluster2Name);

        // Create 2 views
        final View view1 = viewTestTools.createViewWithCluster(view1Name, cluster1);
        view1.setTopic(view1Topic);
        viewTestTools.save(view1);

        final View view2 = viewTestTools.createViewWithCluster(view2Name, cluster2);
        view2.setTopic(view2Topic);
        viewTestTools.save(view2);

        // Users permissions
        final Permissions[] permissions = {
            Permissions.VIEW_READ
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit the index page.
        mockMvc
            .perform(get("/view/")
                .with(user(user)))
            //.andDo(print())
            .andExpect(status().isOk())
            // Should contain this text
            .andExpect(content().string(containsString(cluster1Name)))
            .andExpect(content().string(containsString("a href=\"/cluster/" + cluster1.getId() + "\"")))
            .andExpect(content().string(containsString(cluster2Name)))
            .andExpect(content().string(containsString("a href=\"/cluster/" + cluster2.getId() + "\"")))
            .andExpect(content().string(containsString(view1Name)))
            .andExpect(content().string(containsString("a href=\"/view/" + view1.getId() + "\"")))
            .andExpect(content().string(containsString("a href=\"/stream/" + view1.getId() + "\"")))
            .andExpect(content().string(containsString(view2Name)))
            .andExpect(content().string(containsString("a href=\"/view/" + view2.getId() + "\"")))
            .andExpect(content().string(containsString("a href=\"/stream/" + view2.getId() + "\"")))
            .andExpect(content().string(containsString(view1Topic)))
            .andExpect(content().string(containsString(view2Topic)))

            // But not this
            .andExpect(content().string(not(containsString(ViewTestTools.ASK_ADMIN_CREATE_VIEW_TEXT))))
            .andExpect(content().string(not(containsString(ClusterTestTools.ASK_ADMIN_CREATE_CLUSTER_TEXT))))
            .andExpect(content().string(not(containsString(ClusterTestTools.NO_CLUSTERS_SETUP_TEXT))))
            .andExpect(content().string(not(containsString(ViewTestTools.NO_VIEWS_SETUP_TEXT))))
            .andExpect(content().string(not(containsString(ViewTestTools.CREATE_VIEW_TEXT))))
            .andExpect(content().string(not(containsString(ViewTestTools.CREATE_VIEW_LINK))));
    }
}
