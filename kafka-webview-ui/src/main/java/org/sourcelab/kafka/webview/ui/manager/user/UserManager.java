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

import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Used for creating new users.
 */
@Component
public class UserManager {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserManager(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Create a new user as registered from website.
     * @param email Email of new user.
     * @param displayName Display name of new user.
     * @param password Password of new user.
     * @return The new user.
     */
    @Deprecated
    public User createNewUser(final String email, final String displayName, final String password, final UserRole userRole) {
        final UserBuilder userBuilder = new UserBuilder();
        userBuilder
            .withEmail(email)
            .withDisplayName(displayName)
            .withPassword(password)
            .withRole(userRole)
            .withIsActive(true);

        // Create them!
        return persistNewUser(userBuilder);
    }

    /**
     * Create a new user as registered from website.
     * @param email Email of new user.
     * @param displayName Display name of new user.
     * @param password Password of new user.
     * @return The new user.
     */
    public User createNewUser(final String email, final String displayName, final String password, final long roleId) {
        final UserBuilder userBuilder = new UserBuilder();
        userBuilder
            .withEmail(email)
            .withDisplayName(displayName)
            .withPassword(password)
            .withRoleId(roleId)
            .withIsActive(true);

        // Create them!
        return persistNewUser(userBuilder);
    }

    /**
     * Given a plaintext string, encode it using the password encoder.
     * @param plaintext plaintext to encode
     * @return encoded/hashed value.
     */
    public String encodePassword(final String plaintext) {
        return passwordEncoder.encode(plaintext);
    }

    /**
     * Handle deleting a user.
     * @param user to remove.
     * @return boolean if successful.
     */
    public boolean deleteUser(final User user) {
        // Hard delete user?  Lets do that for now.
        userRepository.delete(user);
        return true;
    }

    private User persistNewUser(final UserBuilder userBuilder) {
        // Build user & save
        final User user = userBuilder.build();
        userRepository.save(user);

        // return the user
        return user;
    }
}
