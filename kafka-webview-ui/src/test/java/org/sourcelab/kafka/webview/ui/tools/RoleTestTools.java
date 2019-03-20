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

package org.sourcelab.kafka.webview.ui.tools;

import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.model.RolePermission;
import org.sourcelab.kafka.webview.ui.repository.RolePermissionRepository;
import org.sourcelab.kafka.webview.ui.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * Helpful tools for Roles.
 */
@Component
public class RoleTestTools {
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Autowired
    public RoleTestTools(final RoleRepository roleRepository, final RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    /**
     * Create a new role with no permissions.
     * @param name Name of the role.
     * @return Role instance.
     */
    public Role createRole(final String name) {
        return createRole(name, Collections.emptyList());
    }

    /**
     * Create a new role with set of permissions.
     * @param name Name of the role.
     * @param permissions Permissions to add.
     * @return Role instance.
     */
    public Role createRole(final String name, Collection<Permissions> permissions) {
        final Role role = new Role();
        role.setName(name);
        roleRepository.save(role);

        permissions.forEach((permission) -> {
            final RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermission(permission.name());
            rolePermissionRepository.save(rolePermission);
        });

        return role;
    }
}
