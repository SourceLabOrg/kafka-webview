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

import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.manager.user.UserManager;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.repository.RoleRepository;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests covering User Configuration Controller.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest extends AbstractMvcTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserManager userManager;

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        // User index page.
        testUrlRequiresAuthentication("/configuration/user", false);

        // User create page.
        testUrlRequiresAuthentication("/configuration/user/create", false);
        testUrlRequiresAuthentication("/configuration/user/create", true);

        // User edit page.
        testUrlRequiresAuthentication("/configuration/user/edit/1", false);
        testUrlRequiresAuthentication("/configuration/user/update", true);

        // User delete page.
        testUrlRequiresAuthentication("/configuration/user/delete/1", true);
    }

    /**
     * Ensure correct permissions are required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthorization() throws Exception {
        // Create at least one user.
        final User user = userTestTools.createUser();

        // User index page.
        testUrlRequiresPermission("/configuration/user", false, Permissions.USER_READ);

        // User create page.
        testUrlRequiresPermission("/configuration/user/create", false, Permissions.USER_CREATE);
        testUrlRequiresPermission("/configuration/user/create", true, Permissions.USER_CREATE);

        // User edit page.
        // TODO sort this out.
//        testUrlRequiresPermission("/configuration/user/edit/" + user.getId(), false, Permissions.USER_MODIFY);
//        testUrlRequiresPermission("/configuration/user/update", true, Permissions.USER_MODIFY);

        // User delete page.
        testUrlRequiresPermission("/configuration/user/delete/1", true, Permissions.USER_DELETE);
    }

    /**
     * Smoke test the user Index page with full user permissions
     */
    @Test
    public void testIndex_allPermissions() throws Exception {
        final Permissions[] permissions = {
            Permissions.USER_READ,
            Permissions.USER_MODIFY,
            Permissions.USER_DELETE,
            Permissions.USER_CREATE
        };

        // Create some users
        final User yourUser = userTestTools.createUserWithPermissions(permissions);
        final User myUser = userTestTools.createUserWithPermissions(permissions);
        final UserDetails user = userTestTools.getUserAuthenticationDetails(myUser);

        // get roles
        final Role myUserRole = roleRepository.findById(myUser.getRoleId()).get();
        final Role yourUserRole = roleRepository.findById(yourUser.getRoleId()).get();

        // Hit index.
        mockMvc
            .perform(get("/configuration/user").with(user(user)))
            .andExpect(status().isOk())
            // Validate user 1
            .andExpect(content().string(containsString(myUser.getEmail())))
            .andExpect(content().string(containsString(myUser.getDisplayName())))
            .andExpect(content().string(containsString(myUserRole.getName())))

            // Validate user 2
            .andExpect(content().string(containsString(yourUser.getEmail())))
            .andExpect(content().string(containsString(yourUser.getDisplayName())))
            .andExpect(content().string(containsString(yourUserRole.getName())))

            // Has Create permission
            .andExpect(content().string(containsString("Create new")))
            .andExpect(content().string(containsString("href=\"/configuration/user/create\"")))
            // Has edit permission
            .andExpect(content().string(containsString("Edit")))
            .andExpect(content().string(containsString("href=\"/configuration/user/edit/" + myUser.getId() + "\"")))
            .andExpect(content().string(containsString("href=\"/configuration/user/edit/" + yourUser.getId() + "\"")))
            // Has delete permission
            .andExpect(content().string(containsString("Delete")))
            .andExpect(content().string(containsString("action=\"/configuration/user/delete/" + yourUser.getId() + "\"")))
            .andExpect(content().string(containsString("action=\"/configuration/user/delete/" + myUser.getId() + "\"")));
    }

    /**
     * Smoke test the user Index page with only read permission.
     */
    @Test
    public void testIndex_readOnlyPermission() throws Exception {
        final Permissions[] permissions = {
            Permissions.USER_READ
        };

        // Create some users
        final User yourUser = userTestTools.createUserWithPermissions(permissions);
        final User myUser = userTestTools.createUserWithPermissions(permissions);
        final UserDetails user = userTestTools.getUserAuthenticationDetails(myUser);

        // get roles
        final Role myUserRole = roleRepository.findById(myUser.getRoleId()).get();
        final Role yourUserRole = roleRepository.findById(yourUser.getRoleId()).get();

        // Hit index.
        mockMvc
            .perform(get("/configuration/user").with(user(user)))
            .andExpect(status().isOk())
            // Validate user 1
            .andExpect(content().string(containsString(myUser.getEmail())))
            .andExpect(content().string(containsString(myUser.getDisplayName())))
            .andExpect(content().string(containsString(myUserRole.getName())))

            // Validate user 2
            .andExpect(content().string(containsString(yourUser.getEmail())))
            .andExpect(content().string(containsString(yourUser.getDisplayName())))
            .andExpect(content().string(containsString(yourUserRole.getName())))

            // Does not have create permission
            .andExpect(content().string(IsNot.not(containsString("Create new"))))
            .andExpect(content().string(IsNot.not(containsString("href=\"/configuration/user/create\""))))
            // Does not have edit permission
            .andExpect(content().string(IsNot.not(containsString("Edit"))))
            .andExpect(content().string(IsNot.not(containsString("href=\"/configuration/user/edit/" + yourUser.getId() + "\""))))
            // But will always have a link to edit their own profile/user
            .andExpect(content().string(containsString("href=\"/configuration/user/edit/" + myUser.getId() + "\"")))
            // Does not have delete permission
            .andExpect(content().string(IsNot.not(containsString("Delete"))))
            .andExpect(content().string(IsNot.not(containsString("action=\"/configuration/user/delete/" + yourUser.getId() + "\""))))
            .andExpect(content().string(IsNot.not(containsString("action=\"/configuration/user/delete/" + myUser.getId() + "\""))));
    }

    /**
     * Smoke test loading create screen.
     */
    @Test
    @Transactional
    public void GetCreate_smokeTestLoadingCreateUser() throws Exception {
        final Permissions[] permissions = {
            Permissions.USER_CREATE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit index.
        mockMvc
            .perform(get("/configuration/user/create")
                .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("New User")))
            // Should submit to the create end point
            .andExpect(content().string(containsString("action=\"/configuration/user/create\"")))
            .andExpect(content().string(containsString("Submit")));
    }

    /**
     * Smoke test loading edit screen for current user, when logged in as a user which
     * has the USER_MODIFY permission.
     */
    @Test
    @Transactional
    public void testLoadEditSelfAsAdminUser() throws Exception {
        final Permissions[] permissions = {
            Permissions.USER_MODIFY,
        };
        final User myUser = userTestTools.createUserWithPermissions(permissions);
        final UserDetails user = userTestTools.getUserAuthenticationDetails(myUser);

        // Hit edit page for same user as logged in with.
        mockMvc
            .perform(get("/configuration/user/edit/" + myUser.getId()).with(user(user)))
            .andExpect(status().isOk())
            // Validate content
            .andExpect(content().string(containsString(myUser.getEmail())))
            .andExpect(content().string(containsString(myUser.getDisplayName())))
            .andExpect(content().string(containsString("value=\"" + myUser.getId() + "\"")))
            // Has ability to re-assign roles on users
            .andExpect(content().string(containsString("User Role")))
            .andExpect(content().string(containsString("id=\"roleId\" name=\"roleId\"")))
            // Validate cancel link
            .andExpect(content().string(containsString("Cancel")))
            .andExpect(content().string(containsString("href=\"/configuration/user\"")))
            // Should submit to the create end point
            .andExpect(content().string(containsString("action=\"/configuration/user/update\"")))
            .andExpect(content().string(containsString("Submit")));
    }

    /**
     * Smoke test loading edit screen for another user, when logged in as a user
     * which has the USER_MODIFY permission.
     */
    @Test
    @Transactional
    public void testLoadEditForOtherUserAsAdminUser() throws Exception {
        final Permissions[] permissions = {
            Permissions.USER_MODIFY,
        };
        final UserDetails userLoginDetails = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create user we want to modify
        final User otherUser = userTestTools.createUser();

        // Hit edit page for same user as logged in with.
        mockMvc
            .perform(get("/configuration/user/edit/" + otherUser.getId()).with(user(userLoginDetails)))
            .andExpect(status().isOk())

            // Validate content
            .andExpect(content().string(containsString(otherUser.getEmail())))
            .andExpect(content().string(containsString(otherUser.getDisplayName())))
            .andExpect(content().string(containsString("value=\"" + otherUser.getId() + "\"")))

            // Has ability to re-assign roles on users
            .andExpect(content().string(containsString("User Role")))
            .andExpect(content().string(containsString("id=\"roleId\" name=\"roleId\"")))

            // Validate cancel link
            .andExpect(content().string(containsString("Cancel")))
            .andExpect(content().string(containsString("href=\"/\"")))

            // Should submit to the create end point
            .andExpect(content().string(containsString("action=\"/configuration/user/update\"")))
            .andExpect(content().string(containsString("Submit")));
    }

    /**
     * Smoke test loading edit screen for current user, when logged in as a user WITHOUT the MODIFY_USER role.
     */
    @Test
    @Transactional
    public void testLoadEditSelfAsNonAdminUser() throws Exception {
        // Does NOT have the USER_MODIFY or USER_READ permision.
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
        };

        final User myUser = userTestTools.createUserWithPermissions(permissions);
        final UserDetails user = userTestTools.getUserAuthenticationDetails(myUser);

        // Hit edit page for same user as logged in with.
        mockMvc
            .perform(get("/configuration/user/edit/" + myUser.getId()).with(user(user)))
            //.andDo(print())
            .andExpect(status().isOk())

            // Validate content
            .andExpect(content().string(containsString(myUser.getEmail())))
            .andExpect(content().string(containsString(myUser.getDisplayName())))
            .andExpect(content().string(containsString("value=\"" + myUser.getId() + "\"")))

            // Should not have User Role editing ability.
            .andExpect(content().string(IsNot.not((containsString("User Role")))))
            .andExpect(content().string(IsNot.not(containsString("id=\"roleId\" name=\"roleId\""))))

            // Validate cancel link
            .andExpect(content().string(containsString("Cancel")))
            .andExpect(content().string(containsString("href=\"/\"")))

            // Should submit to the create end point
            .andExpect(content().string(containsString("action=\"/configuration/user/update\"")))
            .andExpect(content().string(containsString("Submit")));
    }

    /**
     * Smoke test loading edit screen for another user, when logged in as a NON-admin user.
     * This should be blocked.
     */
    @Test
    @Transactional
    public void testLoadEditAnotherUserAsNonAdminUserIsBlocked() throws Exception {
        // Does NOT have the USER_MODIFY or USER_READ permision.
        final Permissions[] permissions = {
            Permissions.CLUSTER_READ,
        };

        final User myUser = userTestTools.createUserWithPermissions(permissions);
        final UserDetails user = userTestTools.getUserAuthenticationDetails(myUser);

        // Create user we want to try to edit
        final User otherUser = userTestTools.createUser();

        // Hit edit page for same user as logged in with.
        mockMvc
            .perform(get("/configuration/user/edit/" + otherUser.getId()).with(user(user)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    /**
     * Test that you cannot update a user by submitting a request to the create end point
     * with an id parameter.
     */
    @Test
    @Transactional
    public void testPostCreate_withId_shouldResultIn400Error() throws Exception {
        final Permissions[] permissions = {
            Permissions.USER_CREATE,
            Permissions.USER_READ,
            Permissions.USER_MODIFY,
            Permissions.USER_DELETE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a role
        final Role role = roleTestTools.createRole("My Test Role");

        // Create a User.
        final String expectedName = "My New User Name " + System.currentTimeMillis();
        final User existingUser = userTestTools.createUser();
        existingUser.setDisplayName(expectedName);

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/user/create")
                .with(user(user))
                .with(csrf())
                // Post the ID parameter to attempt to update existing filter via create end point.
                .param("id", String.valueOf(existingUser.getId()))
                .param("email", "mytest@email.com")
                .param("displayName", "Updated Name")
                .param("password", "not-a-real-password")
                .param("password2", "not-a-real-password")
                .param("roleId", String.valueOf(role.getId())))
            .andExpect(status().is4xxClientError());

        // Lookup User
        final User updatedUser = userRepository.findById(existingUser.getId()).get();
        assertNotNull("Should have user", updatedUser);
        assertEquals("Has original name -- was not updated", expectedName, updatedUser.getDisplayName());
    }

    /**
     * Test that you cannot create a user by submitting a request to the update end point
     * without an id parameter.
     */
    @Test
    @Transactional
    public void testPostUpdate_withOutId_shouldResultIn400Error() throws Exception {
        final Permissions[] permissions = {
            Permissions.USER_CREATE,
            Permissions.USER_READ,
            Permissions.USER_MODIFY,
            Permissions.USER_DELETE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Define a name
        final String expectedName = "My New Name " + System.currentTimeMillis();
        final String expectedEmail = "My" + System.currentTimeMillis() + "@testemail.com";

        // Create a role
        final Role role = roleTestTools.createRole("My Test Role");

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/user/update")
                .with(user(user))
                .with(csrf())
                // Don't include the Id Parameter in this request
                .param("email", expectedEmail)
                .param("displayName", expectedName)
                .param("password", "not-a-real-password")
                .param("password2", "not-a-real-password")
                .param("roleId", String.valueOf(role.getId())))
            .andExpect(status().is4xxClientError());

        // Lookup User
        final User updatedUser = userRepository.findByEmail(expectedEmail);
        assertNull("Should not have user", updatedUser);
    }

    /**
     * Test creating new user.
     */
    @Test
    @Transactional
    public void testPostCreate_newUser() throws Exception {
        final Permissions[] permissions = {
            Permissions.USER_CREATE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final Role role = roleTestTools.createRole("My Test Role " + System.currentTimeMillis());

        final String expectedName = "My New User Name " + System.currentTimeMillis();
        final String expectedEmail = "user" + System.currentTimeMillis() + "@example.com";
        final long expectedRoleId = role.getId();

        // Hit Update end point.
        final MvcResult result = mockMvc
            .perform(multipart("/configuration/user/create")
                .with(user(user))
                .with(csrf())
                .param("email", expectedEmail)
                .param("displayName", expectedName)
                .param("password", "not-a-real-password")
                .param("password2", "not-a-real-password")
                .param("roleId", String.valueOf(expectedRoleId)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/user"))
            .andReturn();

        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        Assert.assertNotNull(flashMessage);
        Assert.assertEquals("success", flashMessage.getType());

        // Lookup user
        final User newUser = userRepository.findByEmail(expectedEmail);
        assertNotNull("Should have new user", newUser);
        assertEquals("Has correct name", expectedName, newUser.getDisplayName());
        assertEquals("Has correct email", expectedEmail, newUser.getEmail());
        assertEquals("Has correct role", (Long) expectedRoleId, newUser.getRoleId());
        assertNotNull("Has a password", newUser.getPassword());

        // Cleanup, lets remove the user.
        userManager.deleteUser(newUser);
    }

    /**
     * Test creating new user with invalid role id.
     */
    @Test
    @Transactional
    public void testPostCreate_newUser_invalidRole() throws Exception {
        final Permissions[] permissions = {
            Permissions.USER_CREATE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String expectedName = "My New User Name " + System.currentTimeMillis();
        final String expectedEmail = "user" + System.currentTimeMillis() + "@example.com";
        final long invalidRoleId = -100L;

        // Hit Update end point.
        mockMvc
            .perform(multipart("/configuration/user/create")
                .with(user(user))
                .with(csrf())
                .param("email", expectedEmail)
                .param("displayName", expectedName)
                .param("password", "not-a-real-password")
                .param("password2", "not-a-real-password")
                .param("roleId", String.valueOf(invalidRoleId)))
            .andExpect(status().isOk())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("userForm", "roleId"));

        // Lookup user
        final User newUser = userRepository.findByEmail(expectedEmail);
        assertNull("Should not have new user", newUser);
    }

    /**
     * Test creating new user with mismatched passwords
     */
    @Test
    @Transactional
    public void testPostCreate_newUserInvalidPassword() throws Exception {
        final Permissions[] permissions = {
            Permissions.USER_CREATE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final Role role = roleTestTools.createRole("My Test Role " + System.currentTimeMillis());

        final String expectedName = "My New User Name " + System.currentTimeMillis();
        final String expectedEmail = "user" + System.currentTimeMillis() + "@example.com";
        final long expectedRoleId = role.getId();

        // Hit Update end point.
        mockMvc
            .perform(multipart("/configuration/user/create")
                .with(user(user))
                .with(csrf())
                .param("email", expectedEmail)
                .param("displayName", expectedName)
                .param("password", "not-a-real-password")
                .param("password2", "mismatched-not-a-real-password")
                .param("roleId", String.valueOf(expectedRoleId)))
            .andExpect(status().isOk())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("userForm", "password"))
            .andExpect(model().attributeHasFieldErrors("userForm", "password2"));

        // Lookup user
        final User newUser = userRepository.findByEmail(expectedEmail);
        assertNull("Should not have new user", newUser);
    }

    // TODO write test cases for edit user, delete user
    // TODO write test case editing own user and attempting to change the role does not work.
    // TODO test cases w/ invalid roleId too.
}