package com.darksci.kafkaview.manager.user;

import com.darksci.kafkaview.manager.email.EmailManager;
import com.darksci.kafkaview.model.User;
import com.darksci.kafkaview.model.UserRole;
import com.darksci.kafkaview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Used for creating new users.
 */
@Component
public class NewUserManager {
    private final UserRepository userRepository;
    private final EmailManager emailManager;

    @Autowired
    public NewUserManager(final UserRepository userRepository, final EmailManager emailManager) {
        this.userRepository = userRepository;
        this.emailManager = emailManager;
    }

    /**
     * Create a new user as registered from website.
     * @param email Email of new user.
     * @param displayName Display name of new user.
     * @param password Password of new user.
     * @return The new user.
     */
    public User createNewUser(final String email, final String displayName, final String password) {
        final UserBuilder userBuilder = new UserBuilder();
        userBuilder
            .withEmail(email)
            .withDisplayName(displayName)
            .withPassword(password)
            .withRole(UserRole.ROLE_USER)
            .withIsActive(true);

        // Create them!
        return persistNewUser(userBuilder);
    }

    private User persistNewUser(final UserBuilder userBuilder) {
        // Build user & save
        final User user = userBuilder.build();
        userRepository.save(user);

        // Now send them a welcome email!
        emailManager.sendWelcomeEmail(user);

        // return the user
        return user;
    }
}
