package org.sourcelab.kafka.webview.ui.tools;

import org.sourcelab.kafka.webview.ui.manager.user.UserBuilder;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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

    private User createNewUser(final UserRole userRole) {
        final User user = new UserBuilder()
            .withDisplayName("Test User")
            .withEmail("test" + System.currentTimeMillis() + "@example.com")
            .withIsActive(true)
            .withPassword("RandomPassword")
            .withRole(userRole)
            .build();

        userRepository.save(user);
        return user;
    }
}
