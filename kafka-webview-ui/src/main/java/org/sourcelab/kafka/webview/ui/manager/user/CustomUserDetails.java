package org.sourcelab.kafka.webview.ui.manager.user;

import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User Details - Represents information about a logged in user.
 */
public class CustomUserDetails implements UserDetails {
    /**
     * The entity that represents the currently logged in user.
     */
    private final User userModel;

    /**
     * List of Roles associated w/ the logged in user.
     */
    private final List<GrantedAuthority> authorities;

    /**
     * Constructor.
     */
    public CustomUserDetails(final User userModel) {
        // set model
        this.userModel = userModel;

        // Generate authorities/roles
        List<GrantedAuthority> roles = new ArrayList<>();

        // Everyone gets user
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Add Admin
        if (UserRole.ROLE_ADMIN.equals(userModel.getRole())) {
            roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        // Save to immutable collection.
        authorities = Collections.unmodifiableList(roles);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return userModel.getPassword();
    }

    @Override
    public String getUsername() {
        return userModel.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isEnabled() {
        return userModel.getActive();
    }

    /**
     * @return The user entity representing the currently logged in user.
     */
    public User getUserModel() {
        return userModel;
    }

    /**
     * @return UserId of the currently logged in user.
     */
    public long getUserId() {
        return getUserModel().getId();
    }
}
