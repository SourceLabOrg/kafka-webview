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

package org.sourcelab.kafka.webview.ui.controller.configuration.role;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.model.RolePermission;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.repository.RolePermissionRepository;
import org.sourcelab.kafka.webview.ui.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests covering Role Configuration Controller.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RoleConfigControllerTest extends AbstractMvcTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        // Role index page.
        testUrlRequiresAuthentication("/configuration/filter", false);

        // Role create page.
        testUrlRequiresAuthentication("/configuration/filter/create", false);
        testUrlRequiresAuthentication("/configuration/filter/create", true);

        // Role edit page.
        testUrlRequiresAuthentication("/configuration/filter/edit/1", false);
        testUrlRequiresAuthentication("/configuration/filter/update", true);

        // Role copy page.
        testUrlRequiresAuthentication("/configuration/role/copy/1", true);

        // Role delete page.
        testUrlRequiresAuthentication("/configuration/filter/delete/1", true);
    }

    /**
     * Ensure correct permissions are required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthorization() throws Exception {
        // Create at least one Role.
        final Role role = roleTestTools.createRole("Test Role " + System.currentTimeMillis());

        // Role index page.
        testUrlRequiresPermission("/configuration/role", false, Permissions.ROLE_READ);

        // Role create page.
        testUrlRequiresPermission("/configuration/role/create", false, Permissions.ROLE_CREATE);
        testUrlRequiresPermission("/configuration/role/create", true, Permissions.ROLE_CREATE);

        // Role edit page.
        testUrlRequiresPermission("/configuration/role/edit/" + role.getId(), false, Permissions.ROLE_MODIFY);
        testUrlRequiresPermission("/configuration/role/update", true, Permissions.ROLE_MODIFY);

        // Role copy page.
        testUrlRequiresPermission("/configuration/role/copy/1", true, Permissions.ROLE_CREATE);

        // Role delete page.
        testUrlRequiresPermission("/configuration/role/delete/1", true, Permissions.ROLE_DELETE);
    }

    /**
     * Smoke test the role Index page.
     */
    @Test
    @Transactional
    public void testIndex() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_READ,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String roleName1 = "Role name 1" + System.currentTimeMillis();
        final String roleName2 = "Role name 2" + System.currentTimeMillis();
        final String roleName3 = "Role name 3" + System.currentTimeMillis();

        // Create 3 roles
        final Role role1 = roleTestTools.createRole(roleName1);
        final Role role2 = roleTestTools.createRole(roleName2);
        final Role role3 = roleTestTools.createRole(roleName3);

        // Create users using those roles
        final User user1 = userTestTools.createUser();
        final User user2 = userTestTools.createUser();
        final User user3 = userTestTools.createUser();
        final User user4 = userTestTools.createUser();
        final User user5 = userTestTools.createUser();

        // Assign the roles
        user1.setRoleId(role1.getId());
        user2.setRoleId(role1.getId());
        user3.setRoleId(role2.getId());
        user4.setRoleId(role2.getId());
        user5.setRoleId(role2.getId());

        // Hit index.
        mockMvc
            .perform(get("/configuration/role").with(user(user)))
            .andExpect(status().isOk())
            // Validate role1
            .andExpect(content().string(containsString(roleName1)))
            .andExpect(content().string(containsString(roleName2)))
            .andExpect(content().string(containsString(roleName3)))
            // Not the best validations...
            .andExpect(content().string(containsString("<td>0</td>")))
            .andExpect(content().string(containsString("<td>2</td>")))
            .andExpect(content().string(containsString("<td>3</td>")))
            // User has full permission so should see create link
            .andExpect(content().string(containsString("Create new")))
            .andExpect(content().string(containsString("href=\"/configuration/role/create\"")));
    }

    /**
     * Smoke test the role create page with appropriate permission.
     */
    @Test
    @Transactional
    public void testLoadCreatePage() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_CREATE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit role create page.
        mockMvc
            .perform(get("/configuration/role/create").with(user(user)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("New User Role")))
            // Should submit to the create end point
            .andExpect(content().string(containsString("action=\"/configuration/role/create\"")))
            .andExpect(content().string(containsString("Submit")));
    }

    /**
     * Test that you cannot update a role by submitting a request to the create end point
     * with an id parameter.
     */
    @Test
    @Transactional
    public void testPostCreate_withId_shouldResultIn400Error() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_MODIFY,
            Permissions.ROLE_CREATE,
            Permissions.ROLE_READ,
            Permissions.ROLE_DELETE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a role.
        final String expectedName = "My New Role Name " + System.currentTimeMillis();
        final Role role = roleTestTools.createRole(expectedName);

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/role/create")
                .with(user(user))
                .with(csrf())
                // Post the ID parameter to attempt to update existing filter via create end point.
                .param("id", String.valueOf(role.getId()))
                .param("name", "Updated Name"))
            .andExpect(status().is4xxClientError());

        // Lookup Role
        final Role updated = roleRepository.findById(role.getId()).get();
        assertNotNull("Should have role", updated);
        assertEquals("Has original name -- was not updated", expectedName, role.getName());
    }

    /**
     * Test that you cannot create a role by submitting a request to the update end point
     * without an id parameter.
     */
    @Test
    @Transactional
    public void testPostUpdate_withOutId_shouldResultIn400Error() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_MODIFY,
            Permissions.ROLE_CREATE,
            Permissions.ROLE_READ,
            Permissions.ROLE_DELETE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // New role name
        final String expectedName = "My New Role Name " + System.currentTimeMillis();

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/role/update")
                .with(user(user))
                .with(csrf())
                // Don't include the Id Parameter in this request
                .param("name", expectedName))
            .andExpect(status().is4xxClientError());

        // Lookup Role
        final Role createdRole = roleRepository.findByName(expectedName);
        assertNull("Should not have role", createdRole);
    }

    /**
     * Test creating new role with permissions set.
     */
    @Test
    @Transactional
    public void testPostCreate_newRole() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_CREATE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String expectedRoleName = "My New Role Name" + System.currentTimeMillis();

        final List<String> expectedPermissions = new ArrayList<>();
        expectedPermissions.add(Permissions.TOPIC_CREATE.name());
        expectedPermissions.add(Permissions.CLUSTER_CREATE.name());
        expectedPermissions.add(Permissions.CLUSTER_DELETE.name());

        // Hit create page.
        final MvcResult result = mockMvc
            .perform(post("/configuration/role/create")
                .with(user(user))
                .with(csrf())
                .param("name", expectedRoleName)
                .param("permissions", Permissions.TOPIC_CREATE.name())
                .param("permissions", Permissions.CLUSTER_CREATE.name())
                .param("permissions", Permissions.CLUSTER_DELETE.name())
            )
            //.andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/role"))
            .andReturn();

        // Get the flash message.
        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        assertNotNull("Should have a flash message defined", flashMessage);
        assertEquals("success", flashMessage.getType());
        assertEquals("Created new role " + expectedRoleName + "!", flashMessage.getMessage());

        // Lookup role
        final Role role = roleRepository.findByName(expectedRoleName);
        assertNotNull("Should have new role", role);

        // Lookup permissions for role
        final Collection<RolePermission> rolePermissions = rolePermissionRepository.findAllByRoleId(role.getId());
        assertEquals("Should have 3 roles", 3, rolePermissions.size());

        for (final RolePermission rolePermission : rolePermissions) {
            assertTrue(expectedPermissions.contains(rolePermission.getPermission()));
        }
    }

    /**
     * Test loading edit role page for existing role.
     */
    @Test
    @Transactional
    public void testGetEdit_loadEditPageForExistingRole() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_MODIFY,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String originalRoleName = "My Original Role Name" + System.currentTimeMillis();

        final List<Permissions> originalPermissions = new ArrayList<>();
        originalPermissions.add(Permissions.TOPIC_CREATE);
        originalPermissions.add(Permissions.CLUSTER_CREATE);
        originalPermissions.add(Permissions.CLUSTER_DELETE);

        // Create role w/ permissions.
        final Role role = roleTestTools.createRole(originalRoleName, originalPermissions);

        // Load edit page.
        mockMvc
            .perform(get("/configuration/role/edit/" + role.getId())
                .with(user(user))
            )
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("value=\"" + originalRoleName + "\"")))
            .andExpect(content().string(containsString("name=\"permissions\" value=\"CLUSTER_DELETE\" checked=\"checked\"")))
            .andExpect(content().string(containsString("name=\"permissions\" value=\"TOPIC_CREATE\" checked=\"checked\"")))
            .andExpect(content().string(containsString("name=\"permissions\" value=\"CLUSTER_DELETE\" checked=\"checked\"")))
            // Should submit to the update end point.
            .andExpect(content().string(containsString("action=\"/configuration/role/update\"")))
            .andExpect(content().string(containsString(
                "<input type=\"hidden\" name=\"id\" id=\"id\" value=\"" + role.getId() + "\">"
            )))
            .andExpect(content().string(containsString("Submit")));
    }

    /**
     * Test updating an existing role.
     */
    @Test
    @Transactional
    public void testPostUpdate_updateExistingRole() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_MODIFY,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String originalRoleName = "My Original Role Name" + System.currentTimeMillis();
        final String expectedRoleName = "My Updated Role Name" + System.currentTimeMillis();

        final List<Permissions> originalPermissions = new ArrayList<>();
        originalPermissions.add(Permissions.TOPIC_CREATE);
        originalPermissions.add(Permissions.CLUSTER_CREATE);
        originalPermissions.add(Permissions.CLUSTER_DELETE);

        final List<String> expectedPermissions = new ArrayList<>();
        expectedPermissions.add(Permissions.TOPIC_CREATE.name());
        expectedPermissions.add(Permissions.CLUSTER_CREATE.name());
        expectedPermissions.add(Permissions.USER_CREATE.name());
        expectedPermissions.add(Permissions.USER_DELETE.name());

        // Create role w/ permissions.
        final Role role = roleTestTools.createRole(originalRoleName, originalPermissions);

        // Post update page.
        final MvcResult result = mockMvc
            .perform(post("/configuration/role/update")
                .with(user(user))
                .with(csrf())
                .param("id", String.valueOf(role.getId()))
                .param("name", expectedRoleName)
                .param("permissions", Permissions.TOPIC_CREATE.name())
                .param("permissions", Permissions.CLUSTER_CREATE.name())
                .param("permissions", Permissions.USER_CREATE.name())
                .param("permissions", Permissions.USER_DELETE.name())
            )
            //.andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/role"))
            .andReturn();

        // Get the flash message.
        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        assertNotNull("Should have a flash message defined", flashMessage);
        assertEquals("success", flashMessage.getType());
        assertEquals("Updated role " + expectedRoleName + "!", flashMessage.getMessage());

        // Lookup role
        final Optional<Role> updatedRole = roleRepository.findById(role.getId());
        assertTrue("Should have role", updatedRole.isPresent());
        assertEquals(expectedRoleName, updatedRole.get().getName());

        // Lookup and validate permissions for role
        final Collection<RolePermission> rolePermissions = rolePermissionRepository.findAllByRoleId(role.getId());
        assertEquals("Should have 4 roles", 4, rolePermissions.size());
        for (final RolePermission rolePermission : rolePermissions) {
            assertTrue(expectedPermissions.contains(rolePermission.getPermission()));
        }
    }

    /**
     * Test copying an existing role.
     */
    @Test
    @Transactional
    public void testPostCopy_copyExistingRole() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_CREATE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String originalRoleName = "My Original Role Name" + System.currentTimeMillis();
        final String expectedRoleName = "Copy of " + originalRoleName;

        final List<Permissions> originalPermissions = new ArrayList<>();
        originalPermissions.add(Permissions.TOPIC_CREATE);
        originalPermissions.add(Permissions.CLUSTER_CREATE);
        originalPermissions.add(Permissions.CLUSTER_DELETE);

        final List<String> expectedPermissions = new ArrayList<>();
        expectedPermissions.add(Permissions.TOPIC_CREATE.name());
        expectedPermissions.add(Permissions.CLUSTER_CREATE.name());
        expectedPermissions.add(Permissions.CLUSTER_DELETE.name());

        // Create role w/ permissions.
        final Role role = roleTestTools.createRole(originalRoleName, originalPermissions);

        // Post copy page.
        final MvcResult result = mockMvc
            .perform(post("/configuration/role/copy/" + role.getId())
                .with(user(user))
                .with(csrf())
            )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/role"))
            .andReturn();

        // Get the flash message.
        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        assertNotNull("Should have a flash message defined", flashMessage);
        assertEquals("success", flashMessage.getType());
        assertEquals("Copied role!", flashMessage.getMessage());

        // Lookup role
        final Optional<Role> updatedRole = roleRepository.findById(role.getId() + 1);
        assertTrue("Should have role", updatedRole.isPresent());
        assertEquals(expectedRoleName, updatedRole.get().getName());

        // Lookup and validate permissions for role
        final Collection<RolePermission> rolePermissions = rolePermissionRepository.findAllByRoleId(role.getId());
        assertEquals("Should have 3 roles", 3, rolePermissions.size());
        for (final RolePermission rolePermission : rolePermissions) {
            assertTrue(expectedPermissions.contains(rolePermission.getPermission()));
        }
    }

    /**
     * Test deleting an existing role that's not in use by any user.
     */
    @Test
    @Transactional
    public void testPostDelete_deleteExistingRole_notInUse() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_DELETE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String originalRoleName = "My Original Role Name" + System.currentTimeMillis();

        final List<Permissions> originalPermissions = new ArrayList<>();
        originalPermissions.add(Permissions.TOPIC_CREATE);
        originalPermissions.add(Permissions.CLUSTER_CREATE);
        originalPermissions.add(Permissions.CLUSTER_DELETE);

        // Create role w/ permissions.
        final Role role = roleTestTools.createRole(originalRoleName, originalPermissions);

        // Post delete page.
        final MvcResult result = mockMvc
            .perform(post("/configuration/role/delete/" + role.getId())
                .with(user(user))
                .with(csrf())
            )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/role"))
            .andReturn();

        // Get the flash message.
        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        assertNotNull("Should have a flash message defined", flashMessage);
        assertEquals("success", flashMessage.getType());
        assertEquals("Deleted role!", flashMessage.getMessage());

        // Lookup role
        final boolean doesExist = roleRepository.existsById(role.getId());
        assertFalse("Should not have role", doesExist);
    }

    /**
     * Test deleting an existing role that IS in use by other users.
     */
    @Test
    @Transactional
    public void testPostDelete_deleteExistingRole_innUse() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_DELETE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String originalRoleName = "My Original Role Name" + System.currentTimeMillis();

        final List<Permissions> originalPermissions = new ArrayList<>();
        originalPermissions.add(Permissions.TOPIC_CREATE);
        originalPermissions.add(Permissions.CLUSTER_CREATE);
        originalPermissions.add(Permissions.CLUSTER_DELETE);

        // Create role w/ permissions.
        final Role role = roleTestTools.createRole(originalRoleName, originalPermissions);

        // Create user
        final User existingUser = userTestTools.createUser();
        existingUser.setRoleId(role.getId());
        userTestTools.save(existingUser);

        // Post delete page.
        final MvcResult result = mockMvc
            .perform(post("/configuration/role/delete/" + role.getId())
                .with(user(user))
                .with(csrf())
            )
            //.andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/role"))
            .andReturn();

        // Get the flash message.
        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        assertNotNull("Should have a flash message defined", flashMessage);
        assertEquals("warning", flashMessage.getType());
        assertEquals("Role in use! Unable to delete!", flashMessage.getMessage());

        // Lookup role
        final boolean doesExist = roleRepository.existsById(role.getId());
        assertTrue("Should have role still", doesExist);
    }

    /**
     * Test removing the Role::READ permission from the current user's role.  It should be blocked.
     */
    @Test
    @Transactional
    public void testPostUpdate_cannotRemoveRoleReadFromYourOwnUsersRole() throws Exception {
        final String roleName = "My Original Role Name" + System.currentTimeMillis();

        final List<Permissions> originalPermissions = new ArrayList<>();
        originalPermissions.add(Permissions.ROLE_CREATE);
        originalPermissions.add(Permissions.ROLE_DELETE);
        originalPermissions.add(Permissions.ROLE_MODIFY);
        originalPermissions.add(Permissions.ROLE_READ);

        final List<String> expectedPermissions = new ArrayList<>();
        expectedPermissions.add(Permissions.ROLE_CREATE.name());
        expectedPermissions.add(Permissions.ROLE_DELETE.name());
        expectedPermissions.add(Permissions.ROLE_MODIFY.name());
        expectedPermissions.add(Permissions.ROLE_READ.name());

        // Create role w/ permissions.
        final Role role = roleTestTools.createRole(roleName, originalPermissions);

        // Create a user using this specific role.
        final User currentUser = userTestTools.createUserWithRole(role);

        // Post update page.
        mockMvc
            .perform(post("/configuration/role/update")
                .with(user(userTestTools.getUserAuthenticationDetails(currentUser)))
                .with(csrf())
                .param("id", String.valueOf(role.getId()))
                .param("name", "Modified Role Name - Shouldnt update")
                .param("permissions", Permissions.ROLE_CREATE.name())
                .param("permissions", Permissions.ROLE_DELETE.name())
                .param("permissions", Permissions.ROLE_MODIFY.name())
            )
            //.andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("You may not remove the permissions Role:Update and/or Role:Read from your current")))
            .andReturn();

        // Lookup role
        final Optional<Role> updatedRole = roleRepository.findById(role.getId());
        assertTrue("Should have role", updatedRole.isPresent());
        assertEquals("Name should be unchanged", roleName, updatedRole.get().getName());

        // Lookup and validate permissions for role
        final Collection<RolePermission> rolePermissions = rolePermissionRepository.findAllByRoleId(role.getId());
        assertEquals("Should still have original 4 roles", 4, rolePermissions.size());
        for (final RolePermission rolePermission : rolePermissions) {
            assertTrue(expectedPermissions.contains(rolePermission.getPermission()));
        }
    }

    /**
     * Test removing the Role::UPDATE permission from the current user's role.  It should be blocked.
     */
    @Test
    @Transactional
    public void testPostUpdate_cannotRemoveRoleUpdateFromYourOwnUsersRole() throws Exception {
        final String roleName = "My Original Role Name" + System.currentTimeMillis();

        final List<Permissions> originalPermissions = new ArrayList<>();
        originalPermissions.add(Permissions.ROLE_CREATE);
        originalPermissions.add(Permissions.ROLE_DELETE);
        originalPermissions.add(Permissions.ROLE_MODIFY);
        originalPermissions.add(Permissions.ROLE_READ);

        final List<String> expectedPermissions = new ArrayList<>();
        expectedPermissions.add(Permissions.ROLE_CREATE.name());
        expectedPermissions.add(Permissions.ROLE_DELETE.name());
        expectedPermissions.add(Permissions.ROLE_MODIFY.name());
        expectedPermissions.add(Permissions.ROLE_READ.name());

        // Create role w/ permissions.
        final Role role = roleTestTools.createRole(roleName, originalPermissions);

        // Create a user using this specific role.
        final User currentUser = userTestTools.createUserWithRole(role);

        // Post update page.
        mockMvc
            .perform(post("/configuration/role/update")
                .with(user(userTestTools.getUserAuthenticationDetails(currentUser)))
                .with(csrf())
                .param("id", String.valueOf(role.getId()))
                .param("name", "Modified Role Name - Shouldnt update")
                .param("permissions", Permissions.ROLE_CREATE.name())
                .param("permissions", Permissions.ROLE_DELETE.name())
                .param("permissions", Permissions.ROLE_READ.name())
            )
            //.andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("You may not remove the permissions Role:Update and/or Role:Read from your current")))
            .andReturn();

        // Lookup role
        final Optional<Role> updatedRole = roleRepository.findById(role.getId());
        assertTrue("Should have role", updatedRole.isPresent());
        assertEquals("Name should be unchanged", roleName, updatedRole.get().getName());

        // Lookup and validate permissions for role
        final Collection<RolePermission> rolePermissions = rolePermissionRepository.findAllByRoleId(role.getId());
        assertEquals("Should still have original 4 roles", 4, rolePermissions.size());
        for (final RolePermission rolePermission : rolePermissions) {
            assertTrue(expectedPermissions.contains(rolePermission.getPermission()));
        }
    }

    /**
     * Test removing the Role::UPDATE permission from a different user's role. It should be OK.
     */
    @Test
    @Transactional
    public void testPostUpdate_canRemoveRoleUpdateAndReadFromOtherUsersRoles() throws Exception {
        final Permissions[] permissions = {
            Permissions.ROLE_MODIFY,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String roleName = "My Original Role Name" + System.currentTimeMillis();

        final List<Permissions> originalPermissions = new ArrayList<>();
        originalPermissions.add(Permissions.ROLE_CREATE);
        originalPermissions.add(Permissions.ROLE_DELETE);
        originalPermissions.add(Permissions.ROLE_MODIFY);
        originalPermissions.add(Permissions.ROLE_READ);

        final List<String> expectedPermissions = new ArrayList<>();
        expectedPermissions.add(Permissions.ROLE_CREATE.name());
        expectedPermissions.add(Permissions.ROLE_DELETE.name());

        // Create role w/ permissions.
        final Role role = roleTestTools.createRole(roleName, originalPermissions);

        // Post update page.
        final MvcResult result = mockMvc
            .perform(post("/configuration/role/update")
                .with(user(user))
                .with(csrf())
                .param("id", String.valueOf(role.getId()))
                .param("name", roleName)
                .param("permissions", Permissions.ROLE_CREATE.name())
                .param("permissions", Permissions.ROLE_DELETE.name())
            )
            //.andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/role"))
            .andReturn();

        // Get the flash message.
        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        assertNotNull("Should have a flash message defined", flashMessage);
        assertEquals("success", flashMessage.getType());
        assertEquals("Updated role " + roleName + "!", flashMessage.getMessage());

        // Lookup role
        final Optional<Role> updatedRole = roleRepository.findById(role.getId());
        assertTrue("Should have role", updatedRole.isPresent());
        assertEquals("Name should be unchanged", roleName, updatedRole.get().getName());

        // Lookup and validate permissions for role
        final Collection<RolePermission> rolePermissions = rolePermissionRepository.findAllByRoleId(role.getId());
        assertEquals("Should still have original 2 roles", 2, rolePermissions.size());
        for (final RolePermission rolePermission : rolePermissions) {
            assertTrue(expectedPermissions.contains(rolePermission.getPermission()));
        }
    }
}