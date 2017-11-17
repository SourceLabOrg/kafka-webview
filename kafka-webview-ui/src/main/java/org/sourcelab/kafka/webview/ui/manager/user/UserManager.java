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

    public String encodePassword(final String plaintext) {
        return passwordEncoder.encode(plaintext);
    }

    private User persistNewUser(final UserBuilder userBuilder) {
        // Build user & save
        final User user = userBuilder.build();
        userRepository.save(user);

        // return the user
        return user;
    }
}
