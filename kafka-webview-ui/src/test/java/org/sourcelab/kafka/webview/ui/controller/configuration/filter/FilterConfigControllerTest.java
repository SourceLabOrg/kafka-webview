package org.sourcelab.kafka.webview.ui.controller.configuration.filter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.configuration.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.tools.FilterTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FilterConfigControllerTest extends AbstractMvcTest {

    @Autowired
    private FilterTestTools filterTestTools;

    /**
     * Test cannot load pages w/o admin role.
     */
    @Test
    @Transactional
    public void test_withoutAdminRole() throws Exception {
        testUrlWithOutAdminRole("/configuration/filter", false);
        testUrlWithOutAdminRole("/configuration/filter/create", false);
        testUrlWithOutAdminRole("/configuration/filter/edit/1", false);
        testUrlWithOutAdminRole("/configuration/filter/update", true);
        testUrlWithOutAdminRole("/configuration/filter/delete/1", true);
    }

    /**
     * Smoke test the Filter Index page.
     */
    @Test
    @Transactional
    public void testIndex() throws Exception {
        // Create some dummy filters
        final Filter filter1 = filterTestTools.createFiler("Filter1");
        final Filter filter2 = filterTestTools.createFiler("Filter2");

        // Hit index.
        mockMvc
            .perform(get("/configuration/filter").with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Validate cluster 1
            .andExpect(content().string(containsString(filter1.getName())))
            .andExpect(content().string(containsString(filter1.getClasspath())))

            // Validate cluster 2
            .andExpect(content().string(containsString(filter2.getName())))
            .andExpect(content().string(containsString(filter2.getClasspath())));
    }
}