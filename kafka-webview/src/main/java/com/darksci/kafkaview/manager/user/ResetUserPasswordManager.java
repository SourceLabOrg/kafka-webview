package com.darksci.kafkaview.manager.user;

import com.darksci.kafkaview.manager.email.EmailManager;
import com.darksci.kafkaview.model.User;
import com.darksci.kafkaview.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * For resetting a user's password.
 */
@Component
public class ResetUserPasswordManager {
    private static final Logger logger = LoggerFactory.getLogger(ResetUserPasswordManager.class);
    private final UserRepository userRepository;
    private final EmailManager emailManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ResetUserPasswordManager(final UserRepository userRepository, final EmailManager emailManager, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailManager = emailManager;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestPasswordReset(final User user) {
        // Generate new password reset hash & save
        user.setResetPasswordHash(UserBuilder.generateRandomHash(user.getEmail()));
        userRepository.save(user);

        // Send email
        emailManager.sendLostPasswordEmail(user);
    }

    public boolean resetPassword(final User user, final String resetToken, final String newPassword) {
        // Sanity checks
        if (user.getResetPasswordHash() == null || user.getResetPasswordHash().isEmpty()) {
            logger.warn("Refusing to reset password for user {} because reset hash is empty or null", user.getEmail());
            return false;
        }
        if (resetToken == null || resetToken.isEmpty()) {
            logger.warn("Refusing to reset password for user {} because reset token is empty or null", user.getEmail());
            return false;
        }

        // Bail if token is invalid
        if (!resetToken.equals(user.getResetPasswordHash())) {
            logger.warn("Refusing to reset password for user {} because reset token is not correct", user.getEmail());
            return false;
        }

        // Otherwise reset password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordHash(null);
        user.setHasPassword(true);
        userRepository.save(user);

        return true;
    }
}
