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

package org.sourcelab.kafka.webview.ui.tools;

import org.sourcelab.kafka.webview.ui.manager.user.UserBuilder;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helpful tools for Users.
 */
@Component
public class UserTestTools {
    private final UserRepository userRepository;

    @Autowired
    public UserTestTools(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new admin user.
     * @return Persisted admin user.
     */
    public User createAdminUser() {
        return createNewUser(UserRole.ROLE_ADMIN);
    }

    /**
     * Creates a new non-admin user.
     * @return Persisted user.
     */
    public User createUser() {
        return createNewUser(UserRole.ROLE_USER);
    }

    /**
     * Easy access to userRepository.
     * @param user User to persist.
     */
    public void save(final User user) {
        userRepository.save(user);
    }

    private User createNewUser(final UserRole userRole) {
        final User user = new UserBuilder()
            .withDisplayName("Test User")
            .withEmail("test" + System.currentTimeMillis() + "@example.com")
            .withIsActive(true)
            .withPassword("RandomPassword")
            .withRole(userRole)
            .build();

        save(user);
        return user;
    }
}
