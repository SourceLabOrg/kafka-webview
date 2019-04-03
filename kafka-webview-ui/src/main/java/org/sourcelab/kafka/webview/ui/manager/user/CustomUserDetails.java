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

import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.RolePermission;
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
     * Constructor when authenticating from local user as defined in database.
     * @param userModel User entity model to authenticate as.
     * @param permissions Collection of permissions associated with this user.
     */
    public CustomUserDetails(final User userModel, final Collection<Permissions> permissions) {
        // set model
        this.userModel = userModel;

        // Generate authorities/roles
        final List<GrantedAuthority> roles = new ArrayList<>();

        // Everyone gets user role
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Loop over permissions and add them as granted authorities for the user.
        permissions.forEach((permission) -> roles.add(new SimpleGrantedAuthority("PERM_" + permission.name())));

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
