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
import org.sourcelab.kafka.webview.ui.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Logic for creating/updating roles.
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

    @Autowired
    public RoleManager(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Create a new role with required entities.
     * @param name Name of the role.
     * @return Role instance.
     */
    public Role createNewRole(final String name) {
        final Role role = new Role();
        role.setName(name);
        roleRepository.save(role);

        return role;
    }

    public Collection<PermissionGroup> getDefaultPermissionGroups() {
        return DEFAULT_PERMISSION_GROUPS;
    }
}
