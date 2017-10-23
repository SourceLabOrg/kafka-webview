package com.darksci.kafka.webview.ui.manager.user;

import com.darksci.kafka.webview.ui.model.UserRole;
import com.darksci.kafka.webview.ui.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public final class UserBuilder {

    private final PasswordEncoder passwordEncoder;

    private String email;
    private String displayName;
    private String password;
    private UserRole role = UserRole.ROLE_USER;
    private String resetPasswordHash = null;
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

    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public UserBuilder withPassword(String password) {
        this.password = password;
        this.hasPassword = true;
        return this;
    }

    public UserBuilder withRole(UserRole role) {
        this.role = role;
        return this;
    }

    public UserBuilder withIsActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

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
        user.setResetPasswordHash(resetPasswordHash);
        user.setActive(isActive);
        user.setHasPassword(hasPassword);
        return user;
    }

    public UserBuilder withRandomPassword() {
        // Generate and store random password
        password = passwordEncoder.encode(generateRandomHash("blah"));
        hasPassword = false;

        return this;
    }

    public static String generateRandomHash(final String salt) {
        // Use random stuff
        final double random = (Math.random() * 31 + System.currentTimeMillis());

        // Concat to the salt and sha1 it
        //return DigestUtils.sha1Hex(salt.concat(String.valueOf(random)));
        return sha1(salt.concat(String.valueOf(random)));

    }

    private static String sha1(final String input) {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(input.getBytes("UTF-8"));
            return byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
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