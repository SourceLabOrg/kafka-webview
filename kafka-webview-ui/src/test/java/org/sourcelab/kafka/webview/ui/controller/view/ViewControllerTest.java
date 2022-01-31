/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
     * Test loading index page with no clusters created, as an admin, you should see a message
     * telling you no clusters exist, and a link to create one.
     */
    @Test
    @Transactional
    public void test_indexAsAdminWithNoClustersShowsCreateClusterLink() throws Exception {
        // Hit the index page.
        mockMvc
            .perform(get("/view/")
                .with(user(adminUserDetails)))
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
     * Test loading index page with no clusters created, as normal user, you should see a message
     * telling you no clusters exist, and text telling you to get an admin to create one.
     */
    @Test
    @Transactional
    public void test_indexAsNonAdminWithNoClustersShowsCreateText() throws Exception {
        // Hit the index page.
        mockMvc
            .perform(get("/view/")
                .with(user(nonAdminUserDetails)))
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
     * Test loading index page with a cluster created, but no views created, as an admin, you should see a message
     * telling you no views exist, and a link to create one.
     */
    @Test
    @Transactional
    public void test_indexAsAdminWithNoViewsShowsCreateClusterLink() throws Exception {
        // Create a cluster
        clusterTestTools.createCluster("My Cluster");

        // Hit the index page.
        mockMvc
            .perform(get("/view/")
                .with(user(adminUserDetails)))
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
     * Test loading index page with a cluster created, but no views created, as a normal user, you should see a message
     * telling you no views exist, and a link to create one.
     */
    @Test
    @Transactional
    public void test_indexAsNonAdminWithNoViewsShowsCreateClusterLink() throws Exception {
        // Create a cluster
        clusterTestTools.createCluster("My Cluster");

        // Hit the index page.
        mockMvc
            .perform(get("/view/")
                .with(user(nonAdminUserDetails)))
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

        // Hit the index page.
        mockMvc
            .perform(get("/view/")
                .with(user(adminUserDetails)))
            //.andDo(print())
            .andExpect(status().isOk())
            // Should contain this text

            // Check for cluster 1
            .andExpect(content().string(containsString(cluster1Name)))
            .andExpect(content().string(containsString("a href=\"/cluster/" + cluster1.getId() + "\"")))

            // Check for cluster 2
            .andExpect(content().string(containsString(cluster2Name)))
            .andExpect(content().string(containsString("a href=\"/cluster/" + cluster2.getId() + "\"")))

            // Check for view 1
            .andExpect(content().string(containsString(view1Name)))
            .andExpect(content().string(containsString("a href=\"/view/" + view1.getId() + "\"")))
            .andExpect(content().string(containsString("a href=\"/stream/" + view1.getId() + "\"")))
            .andExpect(content().string(containsString(view1Topic)))

            // Check for view 2
            .andExpect(content().string(containsString(view2Name)))
            .andExpect(content().string(containsString("a href=\"/view/" + view2.getId() + "\"")))
            .andExpect(content().string(containsString("a href=\"/stream/" + view2.getId() + "\"")))
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
