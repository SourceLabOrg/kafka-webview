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
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetails;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetailsService;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.tools.RoleTestTools;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.AdditionalMatchers.not;
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
    @Deprecated
    protected User adminUser;
    @Deprecated
    protected UserDetails adminUserDetails;

    @Deprecated
    protected User nonAdminUser;
    @Deprecated
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
    @Deprecated
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
        final MockHttpServletRequestBuilder action = buildEndpoint(url, isPost);

        mockMvc
            .perform(action)
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    /**
     * Utility method to test URLs require user authentication to be accessed.
     * @param url Url to hit
     * @param isPost If its a POST true, false if GET
     * @throws Exception on error.
     */
    protected void testUrlRequiresPermission(
        final String url,
        final boolean isPost,
        final Permissions ... requiredPermissions
    ) throws Exception {

        // Create a user with the required permissions.
        final User userWithPermission = userTestTools.createUserWithPermissions(requiredPermissions);
        final CustomUserDetails userWithPermissionsDetails = new CustomUserDetails(userWithPermission, Arrays.asList(requiredPermissions));

        // Create a user WITHOUT the required permissions.
        final User userWithOutPermission = userTestTools.createUserWithPermissions();
        final CustomUserDetails userWithOutPermissionsDetails = new CustomUserDetails(userWithOutPermission, Collections.emptyList());

        // Define end point we want to hit
        final MockHttpServletRequestBuilder action = buildEndpoint(url, isPost);

        // First verify you can hit the URL with the required permission.
        // Since we may not be passing a valid request, just make sure its not forbidden response code.
        // Then verify you cannot hit the URL w/o the required permission(s).
        final MvcResult result = mockMvc
            .perform(action.with(user(userWithOutPermissionsDetails)))
            .andReturn();
        assertNotEquals(HttpStatus.FORBIDDEN, result.getResponse().getStatus());

        // Then verify you cannot hit the URL w/o the required permission(s).
        mockMvc
            .perform(action.with(user(userWithOutPermissionsDetails)))
            .andExpect(status().isForbidden());

        // If we have multiple required permissions, ensure they all have to be AND'd together.
        if (requiredPermissions.length > 1) {
            for (final Permissions requiredPermission : requiredPermissions) {
                // Create a user using only ONE of the required permissions
                final User userWithSinglePermission = userTestTools.createUserWithPermissions(requiredPermission);
                final CustomUserDetails userDetailsWithSinglePermission = new CustomUserDetails(
                    userWithSinglePermission,
                    Collections.singleton(requiredPermission)
                );

                // Attempt to hit the end point, verify it is restricted.
                mockMvc
                    .perform(action.with(user(userDetailsWithSinglePermission)))
                    .andExpect(status().isForbidden());

            }
        }
    }

    private MockHttpServletRequestBuilder buildEndpoint(final String url, final boolean isPost) {
        // Define end point we want to hit
        final MockHttpServletRequestBuilder action;
        if (isPost) {
            action = post(url)
                .with(csrf());
        } else {
            action = get(url);
        }

        return action;
    }
}
