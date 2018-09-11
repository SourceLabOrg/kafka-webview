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

import org.sourcelab.kafka.webview.ui.manager.encryption.Sha1Tools;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utility class for building new User entity instances.
 */
public final class UserBuilder {

    /**
     * Used to encode the passwords using one-way hash algorithm.
     */
    private final PasswordEncoder passwordEncoder;

    private String email;
    private String displayName;
    private String password;
    private UserRole role = UserRole.ROLE_USER;
    private boolean isActive = true;
    private boolean hasPassword = false;

    /**
     * Default constructor, uses BCryptPassword encoder.
     */
    public UserBuilder() {
        this(new BCryptPasswordEncoder());
    }

    /**
     * For injecting alternative password encoders.
     */
    public UserBuilder(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Set email value.
     */
    public UserBuilder withEmail(final String email) {
        this.email = email;
        return this;
    }

    /**
     * Set Display Name value.
     */
    public UserBuilder withDisplayName(final String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Set plain text password value.
     */
    public UserBuilder withPassword(final String password) {
        this.password = password;
        this.hasPassword = true;
        return this;
    }

    /**
     * Set User's Role.
     */
    public UserBuilder withRole(final UserRole role) {
        this.role = role;
        return this;
    }

    /**
     * Set if the user is active or not.
     */
    public UserBuilder withIsActive(final Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    /**
     * Build the entity.
     * @return Entity
     */
    public User build() {
        // Ensure non-null/empty password
        if (password == null || password.isEmpty()) {
            withRandomPassword();
        }

        final User user = new User();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setResetPasswordHash(null);
        user.setActive(isActive);
        user.setHasPassword(hasPassword);
        return user;
    }

    /**
     * Sets a random password.
     */
    private UserBuilder withRandomPassword() {
        // Generate and store random password
        password = passwordEncoder.encode(generateRandomHash("blah"));
        hasPassword = false;

        return this;
    }

    private static String generateRandomHash(final String salt) {
        // Use random stuff
        final double random = (Math.random() * 31 + System.currentTimeMillis());

        // Concat to the salt and sha1 it
        return Sha1Tools.sha1(salt.concat(String.valueOf(random)));

    }
}