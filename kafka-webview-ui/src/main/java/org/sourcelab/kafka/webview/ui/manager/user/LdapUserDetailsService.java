/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.configuration.LdapAppProperties;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Maps from an authenticated LDAP user to a Kafka Webview authenticated user.
 */
public class LdapUserDetailsService implements UserDetailsContextMapper {
    private static final Logger logger = LoggerFactory.getLogger(LdapUserDetailsService.class);

    /**
     * Configured list of LDAP groups that should be considered administrations.
     */
    private final Set<String> adminUserGroups;

    /**
     * Configured list of LDAP groups that should be considered users.
     */
    private final Set<String> userGroups;

    /**
     * Constructor.
     * @param ldapAppProperties configured Ldap application properties.
     */
    public LdapUserDetailsService(final LdapAppProperties ldapAppProperties) {
        final Set<String> tempAdminGroups = new HashSet<>();
        final Set<String> tempUserGroups = new HashSet<>();

        // We want to loop over each configured group and force it to upper case
        // We'll match groups case insensitively.
        ldapAppProperties.getAdminGroups().forEach(
            (groupName) -> tempAdminGroups.add(groupName.toUpperCase())
        );
        ldapAppProperties.getUserGroups().forEach(
            (groupName) -> tempUserGroups.add(groupName.toUpperCase())
        );
        this.adminUserGroups = Collections.unmodifiableSet(tempAdminGroups);
        this.userGroups = Collections.unmodifiableSet(tempUserGroups);
    }

    /**
     * Creates a user details instance from the authenticated ldap user.
     * @param ctx context.
     * @param username ldap username.
     * @param authorities ldap groups the user is a member of.
     * @return CustomUserDetails instance representing this user.
     * @throws BadCredentialsException for users which are not a member of any authorized group.
     */
    @Override
    public UserDetails mapUserFromContext(
        final DirContextOperations ctx,
        final String username,
        final Collection<? extends GrantedAuthority> authorities
    ) {
        // Determine which role to use
        final Collection<UserRole> userRoles = mapUserRole(authorities);

        // If not a member of user or admin user groups
        if (userRoles.isEmpty()) {
            logger.warn("User {} is not a member of any user groups configured for access.", username);

            // Throw exception to block authentication.
            throw new BadCredentialsException(
                "User '" + username
                + "' authenticated with LDAP, but denied access because not a member of any approved user groups."
            );
        }

        // Create a mock user.
        final User ldapUser = new User();
        ldapUser.setId(0);
        ldapUser.setDisplayName(username);
        ldapUser.setEmail(username);

        // Set appropriate role based on granted authority.
        if (userRoles.contains(UserRole.ROLE_ADMIN)) {
            ldapUser.setRole(UserRole.ROLE_ADMIN);
        } else {
            ldapUser.setRole(UserRole.ROLE_USER);
        }

        ldapUser.setActive(true);

        // Return our user details instance.
        return new CustomUserDetails(ldapUser);
    }

    /**
     * Helper method to help map from configured admin and user roles to our internal admin and user roles.
     * @param authorities The groups the user is a member of as determined from LDAP.
     * @return Set of local authorities this user should be granted to kafka webview.
     *      returning empty set will mean that no access should be granted.
     */
    private Collection<UserRole> mapUserRole(final Collection<? extends GrantedAuthority> authorities) {
        final Set<UserRole> addedUserRoles = new HashSet<>();

        // Check admin role
        for (final GrantedAuthority authority : authorities) {
            if (adminUserGroups.contains(authority.getAuthority().toUpperCase())) {
                addedUserRoles.add(UserRole.ROLE_ADMIN);
                addedUserRoles.add(UserRole.ROLE_USER);
                return addedUserRoles;
            } else if (userGroups.contains(authority.getAuthority().toUpperCase())) {
                addedUserRoles.add(UserRole.ROLE_USER);
            }
        }
        return addedUserRoles;
    }

    @Override
    public void mapUserToContext(final UserDetails user, final DirContextAdapter ctx) {
    }
}
