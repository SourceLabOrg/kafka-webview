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

package org.sourcelab.kafka.webview.ui.controller;

import org.junit.Before;
import org.sourcelab.kafka.webview.ui.configuration.AppProperties;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetailsService;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.tools.RoleTestTools;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractMvcTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserTestTools userTestTools;

    @Autowired
    protected RoleTestTools roleTestTools;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    protected AppProperties appProperties;

    /**
     * Authentication details.
     */
    protected User adminUser;
    protected UserDetails adminUserDetails;

    protected User nonAdminUser;
    protected UserDetails nonAdminUserDetails;

    /**
     * Where JKS files are uploaded to.
     */
    protected String uploadPath;

    /**
     * Setup users for MVC tests.
     */
    @Before
    public void setupAuth() {
        // Create user details for logged in user.
        adminUser = userTestTools.createAdminUser();
        adminUserDetails = customUserDetailsService.loadUserByUsername(adminUser.getEmail());

        // Create non-admin user
        nonAdminUser = userTestTools.createUser();
        nonAdminUserDetails = customUserDetailsService.loadUserByUsername(nonAdminUser.getEmail());

        // Define upload path
        uploadPath = appProperties.getUploadPath();
    }

    /**
     * Utility method to test URLs are/are not accessible w/o the admin role.
     * @param url Url to hit
     * @param isPost If its a POST true, false if GET
     * @throws Exception on error.
     */
    protected void testUrlWithOutAdminRole(final String url, final boolean isPost) throws Exception {
        final MockHttpServletRequestBuilder action;
        if (isPost) {
            action = post(url)
                .with(csrf());
        } else {
            action = get(url);
        }

        mockMvc
            .perform(action.with(user(nonAdminUserDetails)))
            //.andDo(print())
            .andExpect(status().isForbidden());
    }

    /**
     * Utility method to test URLs require user authentication to be accessed.
     * @param url Url to hit
     * @param isPost If its a POST true, false if GET
     * @throws Exception on error.
     */
    protected void testUrlRequiresAuthentication(final String url, final boolean isPost) throws Exception {
        final MockHttpServletRequestBuilder action;
        if (isPost) {
            action = post(url)
                .with(csrf());
        } else {
            action = get(url);
        }

        mockMvc
            .perform(action)
            //.andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }
}
