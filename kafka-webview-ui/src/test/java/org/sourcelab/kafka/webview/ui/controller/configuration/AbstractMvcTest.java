package org.sourcelab.kafka.webview.ui.controller.configuration;

import org.junit.Before;
import org.sourcelab.kafka.webview.ui.configuration.AppProperties;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetailsService;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

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
}
