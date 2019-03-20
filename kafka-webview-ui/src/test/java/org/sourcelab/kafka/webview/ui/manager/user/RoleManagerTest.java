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

package org.sourcelab.kafka.webview.ui.manager.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for RoleManager functionality.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RoleManagerTest {

    @Autowired
    private RoleManager roleManager;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Super high level sanity check that this method returns non-empty collection.
     */
    @Test
    public void testSanityGetDefaultPermissionGroups() {
        assertNotNull(roleManager.getDefaultPermissionGroups());
        assertFalse(roleManager.getDefaultPermissionGroups().isEmpty());
    }

    /**
     * Test you can create new roles.
     */
    @Test
    @Transactional
    public void testCreateNewRole() {
        final String roleName = "My New Test Role " + System.currentTimeMillis();

        // Verify role does not exist.
        final Role previouslyExistingRole = roleRepository.findByName(roleName);
        assertNull("Should be null", previouslyExistingRole);

        // Create new role
        final Role newRole = roleManager.createNewRole(roleName);
        assertNotNull("Should return a role", newRole);
        assertEquals(roleName, newRole.getName());

        // Find by name
        final Role foundRoleByName = roleRepository.findByName(roleName);
        assertNotNull("Should not be null", foundRoleByName);
        assertEquals(newRole.getId(), foundRoleByName.getId());
        assertEquals(newRole.getName(), foundRoleByName.getName());
    }

    /**
     * Test you cannot create duplicate named roles.
     */
    @Test(expected = DuplicateRoleException.class)
    @Transactional
    public void testCreateNewRole_duplicateName() {
        final String roleName = "My New Test Role " + System.currentTimeMillis();

        // Verify role does not exist.
        final Role previouslyExistingRole = roleRepository.findByName(roleName);
        assertNull("Should be null", previouslyExistingRole);

        // Create new role
        final Role newRole = roleManager.createNewRole(roleName);
        assertNotNull("Should return a role", newRole);
        assertEquals(roleName, newRole.getName());

        // Attempt to re-create the role
        roleManager.createNewRole(roleName);
    }

    /**
     * Test adding new permissions to existing role.
     */
    @Test
    @Transactional
    public void testUpdatePermissions_addNew() {
        // Create new role
        final String roleName = "My New Test Role " + System.currentTimeMillis();
        final Role newRole = roleManager.createNewRole(roleName);

        // sanity test has no permissions yet
        assertTrue(roleManager.getPermissionsForRole(newRole.getId()).isEmpty());

        // Define permissions
        final List<Permissions> permissionsList = new ArrayList<>();
        permissionsList.add(Permissions.CLUSTER_CREATE);
        permissionsList.add(Permissions.CLUSTER_DELETE);
        permissionsList.add(Permissions.CLUSTER_MODIFY);

        // Update the permissions
        roleManager.updatePermissions(newRole.getId(), permissionsList);

        // Validate.
        validateExpectedPermissions(permissionsList, roleManager.getPermissionsForRole(newRole.getId()));
    }

    /**
     * Test adding new permissions to existing role.
     */
    @Test
    @Transactional
    public void testUpdatePermissions_removeExisting() {
        // Create new role
        final String roleName = "My New Test Role " + System.currentTimeMillis();
        final Role newRole = roleManager.createNewRole(roleName);

        // sanity test has no permissions yet
        assertTrue(roleManager.getPermissionsForRole(newRole.getId()).isEmpty());

        // Define permissions
        final List<Permissions> permissionsList = new ArrayList<>();
        permissionsList.add(Permissions.CLUSTER_CREATE);
        permissionsList.add(Permissions.CLUSTER_DELETE);
        permissionsList.add(Permissions.CLUSTER_MODIFY);
        permissionsList.add(Permissions.CLUSTER_READ);

        // Update the permissions
        roleManager.updatePermissions(newRole.getId(), permissionsList);

        // Validate.
        validateExpectedPermissions(permissionsList, roleManager.getPermissionsForRole(newRole.getId()));

        // Now remove two, add two
        final List<Permissions> updatedPermissionList = new ArrayList<>();
        updatedPermissionList.add(Permissions.CLUSTER_CREATE);
        updatedPermissionList.add(Permissions.CLUSTER_DELETE);
        updatedPermissionList.add(Permissions.TOPIC_CREATE);
        updatedPermissionList.add(Permissions.TOPIC_MODIFY);

        roleManager.updatePermissions(newRole.getId(), updatedPermissionList);
        validateExpectedPermissions(updatedPermissionList, roleManager.getPermissionsForRole(newRole.getId()));
    }

    /**
     * Helper method.
     * @param expectedPermissions Permissions we expect to have.
     * @param actualPermissions Actual permission set.
     */
    private void validateExpectedPermissions(final List<Permissions> expectedPermissions, final Collection<Permissions> actualPermissions) {
        assertEquals(expectedPermissions.isEmpty(), actualPermissions.isEmpty());
        assertEquals(expectedPermissions.size(), actualPermissions.size());

        Collection<Permissions> extraValues = expectedPermissions
            .stream()
            .filter(actualPermissions::contains)
            .collect(Collectors.toList());
        assertFalse("Should have no extra entries", extraValues.isEmpty());

        extraValues = actualPermissions
            .stream()
            .filter(expectedPermissions::contains)
            .collect(Collectors.toList());
        assertFalse("Should have no extra entries", extraValues.isEmpty());

    }
}