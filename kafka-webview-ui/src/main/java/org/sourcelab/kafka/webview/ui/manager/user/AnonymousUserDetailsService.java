/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Simple implementation that should only be used when real user authentication has been disabled.
 */
public class AnonymousUserDetailsService implements UserDetailsService {
    private static CustomUserDetails defaultUserDetails;

    {
        // Setup a mock user.
        final User anonymousUser = new User();
        anonymousUser.setId(0);
        anonymousUser.setDisplayName("Anonymous User");
        anonymousUser.setEmail("Anonymous User");
        anonymousUser.setRole(UserRole.ROLE_ADMIN);
        anonymousUser.setActive(true);

        defaultUserDetails = new CustomUserDetails(anonymousUser);
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return getDefaultAnonymousUser();
    }

    /**
     * @return Default user when not using real user authentication.
     */
    public static CustomUserDetails getDefaultAnonymousUser() {
        return defaultUserDetails;
    }
}
