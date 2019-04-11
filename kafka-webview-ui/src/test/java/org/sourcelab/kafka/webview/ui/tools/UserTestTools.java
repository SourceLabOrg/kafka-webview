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

import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetailsService;
import org.sourcelab.kafka.webview.ui.manager.user.UserBuilder;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helpful tools for Users.
 */
@Component
public class UserTestTools {
    private final UserRepository userRepository;
    private final RoleTestTools roleTestTools;

    @Autowired
    public UserTestTools(final UserRepository userRepository, final RoleTestTools roleTestTools) {
        this.userRepository = userRepository;
        this.roleTestTools = roleTestTools;
    }

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Creates a new admin user.
     * @return Persisted admin user.
     */
    public User createAdminUser() {
        // Create a user with all permissions.
        return createUserWithPermissions(Permissions.values());
    }

    /**
     * Creates a new non-admin user with standard permissions.
     * For backwards compatibility.
     * @return Persisted user.
     */
    public User createUser() {
        // Create user with "standard user" permissions.
        return createUserWithPermissions(
            Permissions.VIEW_READ,
            Permissions.CLUSTER_READ,
            Permissions.TOPIC_READ,
            Permissions.CONSUMER_READ,
            Permissions.USER_READ
        );
    }

    /**
     * Create a new user with the given role.
     * @param role role to apply to user.
     * @return user instance.
     */
    public User createUserWithRole(final Role role) {
        return createNewUser(role.getId());
    }

    /**
     * Create a new user with the given permissions.
     * @param permissions permissions to apply to user.
     * @return user instance.
     */
    public User createUserWithPermissions(final Permissions ... permissions) {
        // Create role
        final Role role = roleTestTools.createRole(
            "Test Standard User Role " + System.currentTimeMillis(),
            permissions
        );

        // Create user
        return createUserWithRole(role);
    }

    /**
     * Create a new user with the given role. Return SpringSecurity user authentication details for the user.
     * @param role role to create user with.
     * @return SpringSecurity user authentication details instance.
     */
    public UserDetails createUserDetailsWithRole(final Role role) {
        return getUserAuthenticationDetails(createUserWithRole(role));
    }

    /**
     * Create a new user with the given permission(s). Return SpringSecurity user authentication details for the user.
     * @param permissions permissions to give to user.
     * @return SpringSecurity user authentication details instance.
     */
    public UserDetails createUserDetailsWithPermissions(final Permissions ... permissions) {
        return getUserAuthenticationDetails(
            createUserWithPermissions(permissions)
        );
    }

    /**
     * Return SpringSecurity user authentication details for the user.
     * @param user use to generate user authentication details for.
     * @return SpringSecurity user authentication details instance.
     */
    public UserDetails getUserAuthenticationDetails(final User user) {
        return customUserDetailsService.loadUserByUsername(user.getEmail());
    }

    /**
     * Easy access to userRepository.
     * @param user User to persist.
     */
    public void save(final User user) {
        userRepository.save(user);
    }

    private User createNewUser(final long roleId) {
        final User user = new UserBuilder()
            .withDisplayName("Test User")
            .withEmail("test" + System.currentTimeMillis() + "@example.com")
            .withIsActive(true)
            .withPassword("RandomPassword")
            .withRoleId(roleId)
            .build();

        save(user);
        return user;
    }
}
