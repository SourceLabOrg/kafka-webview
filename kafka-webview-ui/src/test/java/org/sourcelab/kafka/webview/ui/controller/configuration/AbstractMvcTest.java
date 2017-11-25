package org.sourcelab.kafka.webview.ui.controller.configuration;

import org.junit.Before;
import org.sourcelab.kafka.webview.ui.configuration.AppProperties;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetailsService;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractMvcTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserTestTools userTestTools;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    protected AppProperties appProperties;

    /**
     * Authentication details.
     */
    protected UserDetails adminUserDetails;
    protected UserDetails userDetails;

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
        adminUserDetails = customUserDetailsService.loadUserByUsername(userTestTools.createAdminUser().getEmail());
        userDetails = customUserDetailsService.loadUserByUsername(userTestTools.createUser().getEmail());

        // Define upload path
        uploadPath = appProperties.getUploadPath();
    }

    /**
     * Utility method to test URLs are/are not accessible w/o the admin role.
     * @param url Url to hit
     * @param isPost If its a POST true, false if GET
     * @throws Exception
     */
    protected void testUrlWithOutAdminRole(final String url, final boolean isPost) throws Exception {
        final MockHttpServletRequestBuilder action;
        if (isPost) {
            action = post(url);
        } else {
            action = get(url);
        }

        mockMvc
            .perform(action.with(user(userDetails)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }
}
