package com.darksci.kafka.webview.ui.manager.user;

import com.darksci.kafka.webview.ui.model.User;
import com.darksci.kafka.webview.ui.model.UserRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

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
        //return DigestUtils.sha1Hex(salt.concat(String.valueOf(random)));
        return sha1(salt.concat(String.valueOf(random)));

    }

    private static String sha1(final String input) {
        try {
            final MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(input.getBytes("UTF-8"));
            return byteToHex(crypt.digest());
        }
        catch (final NoSuchAlgorithmException | UnsupportedEncodingException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    private static String byteToHex(final byte[] hash) {
        try (final Formatter formatter = new Formatter();) {
            for (final byte bit : hash) {
                formatter.format("%02x", bit);
            }
            return formatter.toString();
        }
    }
}