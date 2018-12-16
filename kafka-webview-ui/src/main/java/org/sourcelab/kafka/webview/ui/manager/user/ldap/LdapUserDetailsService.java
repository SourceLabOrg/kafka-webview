package org.sourcelab.kafka.webview.ui.manager.user.ldap;

import org.sourcelab.kafka.webview.ui.configuration.LdapAppProperties;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetails;
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

public class LdapUserDetailsService implements UserDetailsContextMapper {
    private final Set<String> adminUserGroups;
    private final Set<String> userGroups;

    public LdapUserDetailsService(final LdapAppProperties ldapAppProperties) {
        // Do case insensitive check on group name.
        final Set<String> tempAdminGroups = new HashSet<>();
        final Set<String> tempUserGroups = new HashSet<>();

        ldapAppProperties.getAdminGroups().forEach(
            (groupName) -> {
                tempAdminGroups.add(groupName.toUpperCase());
            }
        );
        ldapAppProperties.getUserGroups().forEach(
            (groupName) -> {
                tempUserGroups.add(groupName.toUpperCase());
            }
        );
        this.adminUserGroups = Collections.unmodifiableSet(tempAdminGroups);
        this.userGroups = Collections.unmodifiableSet(tempUserGroups);
    }

    @Override
    public UserDetails mapUserFromContext(final DirContextOperations ctx, final String username, final Collection<? extends GrantedAuthority> authorities) {
        // Determine which role to use
        final Collection<UserRole> userRoles = mapUserRole(authorities);

        // If not a member of user or admin user groups
        if (userRoles.isEmpty()) {
            // TODO log an error here?
            
            // Throw exception.
            throw new BadCredentialsException("User " + username + " is not a member of any approved user groups!");
        }

        // Setup a mock user.
        final User ldapUser = new User();
        ldapUser.setId(0);
        ldapUser.setDisplayName(username);
        ldapUser.setEmail(username);
        ldapUser.setRole(null);
        ldapUser.setActive(true);
        return new CustomUserDetails(ldapUser, userRoles);
    }

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
