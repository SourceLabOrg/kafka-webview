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

package org.sourcelab.kafka.webview.ui.controller.configuration.filter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.repository.FilterRepository;
import org.sourcelab.kafka.webview.ui.tools.FilterTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FilterConfigControllerTest extends AbstractMvcTest {

    @Autowired
    private FilterTestTools filterTestTools;

    @Autowired
    private FilterRepository filterRepository;

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        // Filter index page.
        testUrlRequiresAuthentication("/configuration/filter", false);

        // Filter create page.
        testUrlRequiresAuthentication("/configuration/filter/create", false);
        testUrlRequiresAuthentication("/configuration/filter/create", true);

        // Filter edit page.
        testUrlRequiresAuthentication("/configuration/filter/edit/1", false);
        testUrlRequiresAuthentication("/configuration/filter/update", true);
    }

    /**
     * Ensure correct permissions are required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthorization() throws Exception {
        // Create at least one filter.
        final Filter filter = filterTestTools.createFilter("Test Filter " + System.currentTimeMillis());

        // Filter index page.
        testUrlRequiresPermission("/configuration/filter", false, Permissions.VIEW_READ);

        // Filter create page.
        testUrlRequiresPermission("/configuration/filter/create", false, Permissions.FILTER_CREATE);
        testUrlRequiresPermission("/configuration/filter/create", true, Permissions.FILTER_CREATE);

        // Filter edit page.
        testUrlRequiresPermission("/configuration/filter/edit/" + filter.getId(), false, Permissions.FILTER_MODIFY);
        testUrlRequiresPermission("/configuration/filter/update", true, Permissions.FILTER_MODIFY);
    }

    /**
     * Smoke test the Filter Index page.
     */
    @Test
    @Transactional
    public void testGetIndex() throws Exception {
        final Permissions[] permissions = {
            Permissions.VIEW_READ,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create some dummy filters
        final Filter filter1 = filterTestTools.createFilter("Filter1");
        final Filter filter2 = filterTestTools.createFilter("Filter2");

        // Hit index.
        mockMvc
            .perform(get("/configuration/filter").with(user(user)))
            //.andDo(print())
            .andExpect(status().isOk())
            // Validate filter 1
            .andExpect(content().string(containsString(filter1.getName())))
            .andExpect(content().string(containsString(filter1.getClasspath())))

            // Validate filter 2
            .andExpect(content().string(containsString(filter2.getName())))
            .andExpect(content().string(containsString(filter2.getClasspath())));
    }

    /**
     * Smoke test the Filter create page.
     */
    @Test
    @Transactional
    public void testGetCreate() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_CREATE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit index.
        mockMvc
            .perform(get("/configuration/filter/create")
                .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("New Filter")))
            // Should submit to the create end point
            .andExpect(content().string(containsString("action=\"/configuration/filter/create\"")))
            .andExpect(content().string(containsString("Submit")));
    }

    /**
     * Smoke test the Filter edit page.
     */
    @Test
    @Transactional
    public void testGetEdit() throws Exception {
        // Create a Filter.
        final Filter filter = filterTestTools.createFilter("Test Filter " + System.currentTimeMillis());

        final Permissions[] permissions = {
            Permissions.FILTER_MODIFY,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit index.
        mockMvc
            .perform(get("/configuration/filter/edit/" + filter.getId())
                .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(filter.getName())))
            // Should submit to the update end point.
            .andExpect(content().string(containsString("action=\"/configuration/filter/update\"")))
            .andExpect(content().string(containsString(
                "<input type=\"hidden\" name=\"id\" id=\"id\" value=\"" + filter.getId() + "\">"
            )))
            .andExpect(content().string(containsString("Submit")));
    }

    /**
     * Test that you cannot update a filter by submitting a request to the create end point
     * with an id parameter.
     */
    @Test
    @Transactional
    public void testPostCreate_withId_shouldResultIn400Error() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_MODIFY,
            Permissions.FILTER_CREATE,
            Permissions.FILTER_DELETE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a Filter.
        final String expectedFilterName = "My New Filter Name " + System.currentTimeMillis();
        final Filter filter = filterTestTools.createFilter(expectedFilterName);

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/filter/create")
                .with(user(user))
                .with(csrf())
                // Post the ID parameter to attempt to update existing filter via create end point.
                .param("id", String.valueOf(filter.getId()))
                .param("name", "Updated Name"))
            .andExpect(status().is4xxClientError());

        // Lookup Filter
        final Filter updatedFilter = filterRepository.findById(filter.getId()).get();
        assertNotNull("Should have filter", updatedFilter);
        assertEquals("Has original name -- was not updated", expectedFilterName, filter.getName());
    }

    /**
     * Test that you cannot create a filter by submitting a request to the update end point
     * without an id parameter.
     */
    @Test
    @Transactional
    public void testPostUpdate_withOutId_shouldResultIn400Error() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_MODIFY,
            Permissions.FILTER_CREATE,
            Permissions.FILTER_DELETE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a Filter.
        final String expectedFilterName = "My New Filter Name " + System.currentTimeMillis();

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/filter/update")
                .with(user(user))
                .with(csrf())
                // Don't include the Id Parameter in this request
                .param("name", expectedFilterName))
            .andExpect(status().is4xxClientError());

        // Lookup Filter
        final Filter createdFilter = filterRepository.findByName(expectedFilterName);
        assertNull("Should not have filter", createdFilter);
    }

    // TODO write test over create/update/delete end points.
}