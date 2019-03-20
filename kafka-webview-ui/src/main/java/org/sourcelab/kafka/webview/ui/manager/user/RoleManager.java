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

import org.sourcelab.kafka.webview.ui.manager.user.permission.PermissionGroup;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.model.RolePermission;
import org.sourcelab.kafka.webview.ui.repository.RolePermissionRepository;
import org.sourcelab.kafka.webview.ui.repository.RoleRepository;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Logic for creating/updating and in general interacting with roles and permissions for roles.
 */
@Component
public class RoleManager {
    private static final Collection<PermissionGroup> DEFAULT_PERMISSION_GROUPS;

    // Define default permission groups.
    static {
        final List<PermissionGroup> permissionGroups = new ArrayList<>();
        permissionGroups.add(new PermissionGroup(
            "Cluster",
            "Management of kafka cluster configuration within Kafka WebView.",
            Arrays.asList(
            Permissions.CLUSTER_CREATE,
            Permissions.CLUSTER_READ,
            Permissions.CLUSTER_MODIFY,
            Permissions.CLUSTER_DELETE
        )));

        permissionGroups.add(new PermissionGroup(
            "View",
            "Management of View configuration within Kafka WebView.",
            Arrays.asList(
            Permissions.VIEW_CREATE,
            Permissions.VIEW_READ,
            Permissions.VIEW_MODIFY,
            Permissions.VIEW_DELETE
        )));

        permissionGroups.add(new PermissionGroup(
            "User",
            "Management of User configuration within Kafka WebView.",
            Arrays.asList(
            Permissions.USER_CREATE,
            Permissions.USER_READ,
            Permissions.USER_MODIFY,
            Permissions.USER_DELETE
        )));

        permissionGroups.add(new PermissionGroup(
            "Role",
            "Management of User Roles configuration within Kafka WebView.",
            Arrays.asList(
            Permissions.ROLE_CREATE,
            Permissions.ROLE_READ,
            Permissions.ROLE_MODIFY,
            Permissions.ROLE_DELETE
        )));

        permissionGroups.add(new PermissionGroup(
            "Topic",
            "Management of Topics within Kafka clusters.",
            Arrays.asList(
            Permissions.TOPIC_CREATE,
            Permissions.TOPIC_READ,
            Permissions.TOPIC_MODIFY,
            Permissions.TOPIC_DELETE
        )));

        permissionGroups.add(new PermissionGroup(
            "Consumer",
            "Management of Consumers within Kafka clusters.",
            Arrays.asList(
            Permissions.CONSUMER_READ,
            Permissions.CONSUMER_MODIFY,
            Permissions.CONSUMER_DELETE
        )));

        DEFAULT_PERMISSION_GROUPS = Collections.unmodifiableCollection(permissionGroups);
    }

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;

    /**
     * Constructor.
     * @param roleRepository role repository instance.
     * @param rolePermissionRepository role permission repository instance.
     * @param userRepository user repository instance.
     */
    @Autowired
    public RoleManager(
        final RoleRepository roleRepository,
        final RolePermissionRepository rolePermissionRepository,
        final UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new role with required entities.
     * @param name Name of the role.
     * @return Role instance.
     * @throws DuplicateRoleException if attempted to create a role with a duplicate name.
     */
    public Role createNewRole(final String name) {
        final Role role = new Role();
        role.setName(name);

        try {
            roleRepository.save(role);
        } catch (final Exception exception) {
            throw new DuplicateRoleException("Role with name '" + name + "' already exists!", exception);
        }

        return role;
    }

    /**
     * Given a Role, update it's set of permissions.  Any permissions not included will be removed from the role.
     * @param roleId id of role to update.
     * @param permissions Collection of permissions to set on that role.
     */
    public void updatePermissions(final long roleId, final Collection<Permissions> permissions) {
        // Retrieve all permissions for the role.
        final Iterable<RolePermission> currentPermissions = rolePermissionRepository.findAllByRoleId(roleId);

        final List<RolePermission> rolePermissionsToRemove = new ArrayList<>();
        final List<RolePermission> rolePermissionsToAdd = new ArrayList<>();

        // Loop over permissions, n*n but I'm lazy, and this a short list.
        for (final Permissions permission : permissions) {
            boolean found = false;
            for (final RolePermission currentPermission : currentPermissions) {
                if (currentPermission.getPermission().equals(permission.name())) {
                    found = true;
                    break;
                }
            }
            // If we didn't find the permission, we need to add it.
            if (!found) {
                final RolePermission newRolePermission = new RolePermission();
                newRolePermission.setRoleId(roleId);
                newRolePermission.setPermission(permission.name());
                rolePermissionsToAdd.add(newRolePermission);
            }
        }

        // Loop the reverse way
        for (final RolePermission currentPermission : currentPermissions) {
            boolean found = false;
            for (final Permissions permission : permissions) {
                if (currentPermission.getPermission().equals(permission.name())) {
                    found = true;
                    break;
                }
            }
            // If we don't find a match
            if (!found) {
                // We need to remove it.
                rolePermissionsToRemove.add(currentPermission);
            }
        }

        // Batch persist.
        rolePermissionRepository.saveAll(rolePermissionsToAdd);
        rolePermissionRepository.deleteAll(rolePermissionsToRemove);
    }

    /**
     * Given a roleId, get the permissions enum values associated with that role.
     * @param roleId Id of role to retrieve permissions for.
     * @return Collection of permissions.
     */
    public Collection<Permissions> getPermissionsForRole(final long roleId) {
        // Find existing role permissions
        final Iterable<RolePermission> rolePermissionEntities = rolePermissionRepository.findAllByRoleId(roleId);

        final List<Permissions> permissions = new ArrayList<>();
        rolePermissionEntities
            .forEach((rolePermissionEntity) -> {
                permissions.add(Permissions.valueOf(rolePermissionEntity.getPermission()));
            });

        return Collections.unmodifiableCollection(permissions);
    }

    /**
     * Get the default available permission set available to be used.
     * @return Immutable collection of available permissions.
     */
    public Collection<PermissionGroup> getDefaultPermissionGroups() {
        return DEFAULT_PERMISSION_GROUPS;
    }

    /**
     * Utility to copy a role.
     * @param sourceRole The role you want to copy.
     * @param name The new name of the role.
     * @return New role instance.
     */
    public Role copyRole(final Role sourceRole, final String name) {

        Role copiedRole = null;

        // Attempt to avoid duplicate errors
        for (int attempt = 0; attempt <= 10; attempt++) {
            String nameAttempt = name;
            if (attempt > 0) {
                nameAttempt = nameAttempt + " (" + attempt + ")";
            }

            // Look for role
            final Role existingRole = roleRepository.findByName(nameAttempt);
            if (existingRole != null) {
                continue;
            }

            // If doesn't exist, create it.
            copiedRole = createNewRole(nameAttempt);
            break;
        }

        updatePermissions(copiedRole.getId(), getPermissionsForRole(sourceRole.getId()));

        return copiedRole;
    }

    /**
     * Delete a role.
     * @param roleId the id of the role to remove.
     * @return False if unable to remove, true if successfully deleted.
     */
    public boolean deleteRole(final long roleId) {
        // See if this role is in use by any users
        if (userRepository.existsByRoleId(roleId)) {
            return false;
        }

        // Delete any permissions associated.
        rolePermissionRepository.deleteByRoleId(roleId);

        // Delete role
        roleRepository.deleteById(roleId);

        return true;
    }
}
