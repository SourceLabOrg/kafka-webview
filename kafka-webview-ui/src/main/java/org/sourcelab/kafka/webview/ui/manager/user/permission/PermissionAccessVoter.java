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

package org.sourcelab.kafka.webview.ui.manager.user.permission;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * For determining if an authenticated user has the required permissions to access a secured item.
 *
 * If the user is NOT authenticated, this class will vote DENIED.
 * If the user has no permissions prefixed with 'PERM_' are found, it will vote ABSTAIN.
 * If the user does not have granted authories for ALL of the required PERM_'s, it will vote DENIED.
 *
 * You need to annotate a method or class with the @RequirePermission annotation.
 * All Permissions defined in the annotation are AND'd together.
 *
 * Example: @RequirePermission({Permissions.VIEW_READ, Permissions.VIEW_DELETE}) would require
 * the user to have BOTH permissions granted.
 */
public class PermissionAccessVoter implements AccessDecisionVoter<Object> {
    private static final String PERMISSION_PREFIX = "PERM_";

    @Override
    public boolean supports(ConfigAttribute attribute) {
        // If the attribute name starts with PERM_, then it's relevant to us.
        return (attribute.getAttribute() != null) && attribute.getAttribute().startsWith(PERMISSION_PREFIX);
    }

    @Override
    public boolean supports(final Class<?> clazz) {
        return true;
    }

    @Override
    public int vote(final Authentication authentication, final Object object, final Collection<ConfigAttribute> attributes) {
        // If we have no authentication, then immediately vote denied.
        if (authentication == null) {
            return ACCESS_DENIED;
        }

        // Default to abstain from voting.
        int result = ACCESS_ABSTAIN;
        final Collection<? extends GrantedAuthority> authorities = extractAuthorities(authentication);
        final Set<String> matchedPermissions = new HashSet<>();
        final Set<String> requiredPermissions = new HashSet<>();

        // Loop over attributes
        for (final ConfigAttribute attribute : attributes) {
            // Skip entries that aren't relevant to us.
            if (!supports(attribute)) {
                continue;
            }

            // Update default to DENIED.
            result = ACCESS_DENIED;

            // Add to our required set
            requiredPermissions.add(attribute.getAttribute());

            // Require matching ALL
            for (final GrantedAuthority authority : authorities) {
                if (attribute.getAttribute().equals(authority.getAuthority())) {
                    matchedPermissions.add(attribute.getAttribute());
                }
            }
        }

        // If we have no relevant entries, abstain.
        if (result == ACCESS_ABSTAIN) {
            return result;
        }

        // If we found ALL required attributes.
        if (!requiredPermissions.isEmpty() && requiredPermissions.equals(matchedPermissions)) {
            // Granted.
            return ACCESS_GRANTED;
        }

        return result;
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(Authentication authentication) {
        return authentication.getAuthorities();
    }
}
