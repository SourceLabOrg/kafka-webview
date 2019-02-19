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

package org.sourcelab.kafka.webview.ui.controller.configuration.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests covering User Configuration Controller.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest extends AbstractMvcTest {

    /**
     * Test cannot load pages w/o admin role.
     */
    @Test
    @Transactional
    public void test_withoutAdminRole() throws Exception {
        testUrlWithOutAdminRole("/configuration/user", false);
        testUrlWithOutAdminRole("/configuration/user/create", false);
        testUrlWithOutAdminRole("/configuration/filter/delete/1", true);
    }

    /**
     * Smoke test the user Index page.
     */
    @Test
    @Transactional
    public void testIndex() throws Exception {
        // Hit index.
        mockMvc
            .perform(get("/configuration/user").with(user(adminUserDetails)))
            //.andDo(print())
            .andExpect(status().isOk())
            // Validate user 1
            .andExpect(content().string(containsString(adminUser.getEmail())))
            .andExpect(content().string(containsString(adminUser.getDisplayName())))

            // Validate user 2
            .andExpect(content().string(containsString(nonAdminUser.getEmail())))
            .andExpect(content().string(containsString(nonAdminUser.getDisplayName())));
    }

    /**
     * Smoke test loading edit screen for current user, when logged in as an Admin user.
     */
    @Test
    @Transactional
    public void testLoadEditSelfAsAdminUser() throws Exception {
        // Hit edit page for same user as logged in with.
        mockMvc
            .perform(get("/configuration/user/edit/" + adminUser.getId()).with(user(adminUserDetails)))
            //.andDo(print())
            .andExpect(status().isOk())

            // Validate content
            .andExpect(content().string(containsString(adminUser.getEmail())))
            .andExpect(content().string(containsString(adminUser.getDisplayName())))
            .andExpect(content().string(containsString("value=\"" + adminUser.getId() + "\"")))

            // Validate cancel link
            .andExpect(content().string(containsString("Cancel")))
            .andExpect(content().string(containsString("href=\"/configuration/user\"")));
    }

    /**
     * Smoke test loading edit screen for another user, when logged in as an Admin user.
     */
    @Test
    @Transactional
    public void testLoadEditForOtherUserAsAdminUser() throws Exception {
        // Hit edit page for same user as logged in with.
        mockMvc
            .perform(get("/configuration/user/edit/" + nonAdminUser.getId()).with(user(adminUserDetails)))
            //.andDo(print())
            .andExpect(status().isOk())

            // Validate content
            .andExpect(content().string(containsString(nonAdminUser.getEmail())))
            .andExpect(content().string(containsString(nonAdminUser.getDisplayName())))
            .andExpect(content().string(containsString("value=\"" + nonAdminUser.getId() + "\"")))

            // Validate cancel link
            .andExpect(content().string(containsString("Cancel")))
            .andExpect(content().string(containsString("href=\"/\"")));
    }

    /**
     * Smoke test loading edit screen for current user, when logged in as a NON-admin user.
     */
    @Test
    @Transactional
    public void testLoadEditSelfAsNonAdminUser() throws Exception {
        // Hit edit page for same user as logged in with.
        mockMvc
            .perform(get("/configuration/user/edit/" + nonAdminUser.getId()).with(user(nonAdminUserDetails)))
            //.andDo(print())
            .andExpect(status().isOk())

            // Validate content
            .andExpect(content().string(containsString(nonAdminUser.getEmail())))
            .andExpect(content().string(containsString(nonAdminUser.getDisplayName())))
            .andExpect(content().string(containsString("value=\"" + nonAdminUser.getId() + "\"")))

            // Validate cancel link
            .andExpect(content().string(containsString("Cancel")))
            .andExpect(content().string(containsString("href=\"/\"")));
    }

    /**
     * Smoke test loading edit screen for another user, when logged in as a NON-admin user.
     * This should be blocked.
     */
    @Test
    @Transactional
    public void testLoadEditAnotherUserAsNonAdminUserIsBlocked() throws Exception {
        // Hit edit page for same user as logged in with.
        mockMvc
            .perform(get("/configuration/user/edit/" + adminUser.getId()).with(user(nonAdminUserDetails)))
            //.andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }
}