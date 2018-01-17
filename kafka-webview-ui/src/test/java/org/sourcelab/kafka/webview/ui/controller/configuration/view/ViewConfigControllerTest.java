package org.sourcelab.kafka.webview.ui.controller.configuration.view;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.configuration.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.plugin.UploadManager;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.FilterTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ViewConfigControllerTest extends AbstractMvcTest {

    @Autowired
    private FilterTestTools filterTestTools;

    @Autowired
    private ViewTestTools viewTestTools;

    @Autowired
    private UploadManager uploadManager;

    /**
     * Test cannot load pages w/o admin role.
     */
    @Test
    @Transactional
    public void test_withoutAdminRole() throws Exception {
        testUrlWithOutAdminRole("/configuration/view", false);
        testUrlWithOutAdminRole("/configuration/view/create", false);
        testUrlWithOutAdminRole("/configuration/view/edit/1", false);
        testUrlWithOutAdminRole("/configuration/view/update", true);
        testUrlWithOutAdminRole("/configuration/view/delete/1", true);
    }

    /**
     * Smoke test the View Index page.
     */
    @Test
    @Transactional
    public void testIndex() throws Exception {
        // Create some dummy filters
        final View view1 = viewTestTools.createView("View 1");
        final View view2 = viewTestTools.createView("View 2");

        // Hit index.
        mockMvc
            .perform(get("/configuration/view").with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Validate view 1
            .andExpect(content().string(containsString(view1.getName())))

            // Validate view 2
            .andExpect(content().string(containsString(view2.getName())));
    }

    /**
     * Smoke test the View create page.
     */
    @Test
    @Transactional
    public void testCreate() throws Exception {
        // Hit index.
        mockMvc
            .perform(get("/configuration/view/create").with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())

            // Validate submit button seems to show up.
            .andExpect(content().string(containsString("type=\"submit\"")));
    }

    /**
     * Quick and dirty smoke test the View create page when we have a filter with options.
     */
    @Test
    @Transactional
    public void testRegressionCreateViewWhenFilterExistsWithOptions() throws Exception {
        // Create filter that has filter options
        final String name = "SearchStringFilter" + System.currentTimeMillis();
        final Filter filter = filterTestTools.createFilterFromTestPlugins(name, "examples.filter.StringSearchFilter");

        // Hit index.
        mockMvc
            .perform(get("/configuration/view/create").with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())

            // Validate submit button seems to show up.
            .andExpect(content().string(containsString("type=\"submit\"")));
    }
}