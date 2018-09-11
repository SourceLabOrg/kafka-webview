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

package org.sourcelab.kafka.webview.ui.controller.configuration.view;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.tools.FilterTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

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