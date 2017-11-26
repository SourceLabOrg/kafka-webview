package org.sourcelab.kafka.webview.ui.controller.configuration.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.configuration.AbstractMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
            .perform(get("/configuration/filter").with(user(adminUserDetails)))
            .andDo(print())
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
            .andDo(print())
            .andExpect(status().isOk())

            // Validate content
            .andExpect(content().string(containsString(adminUser.getEmail())))
            .andExpect(content().string(containsString(adminUser.getDisplayName())))
            .andExpect(content().string(containsString("value=\"" + adminUser.getId() + "\"")));
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
            .andDo(print())
            .andExpect(status().isOk())

            // Validate content
            .andExpect(content().string(containsString(nonAdminUser.getEmail())))
            .andExpect(content().string(containsString(nonAdminUser.getDisplayName())))
            .andExpect(content().string(containsString("value=\"" + nonAdminUser.getId() + "\"")));
    }

    /**
     * Smoke test loading edit screen for current user, when logged in as a NON-admin user.
     */
    @Test
    @Transactional
    public void testLoadEditSelfAsNonAdminUser() throws Exception {
        // Hit edit page for same user as logged in with.
        mockMvc
            .perform(get("/configuration/user/edit/" + nonAdminUser.getId()).with(user(nonAdminUserDetials)))
            .andDo(print())
            .andExpect(status().isOk())

            // Validate content
            .andExpect(content().string(containsString(nonAdminUser.getEmail())))
            .andExpect(content().string(containsString(nonAdminUser.getDisplayName())))
            .andExpect(content().string(containsString("value=\"" + nonAdminUser.getId() + "\"")));
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
            .perform(get("/configuration/user/edit/" + adminUser.getId()).with(user(nonAdminUserDetials)))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }
}