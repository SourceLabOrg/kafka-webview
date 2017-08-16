package com.dsc.wai.manager.user;

import com.dsc.wai.model.User;
import com.dsc.wai.model.UserRole;
import com.dsc.wai.model.UserSource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class UserBuilder {

    private final PasswordEncoder passwordEncoder;

    private String email;
    private String displayName;
    private String password;
    private UserRole role = UserRole.ROLE_USER;
    private String resetPasswordHash = null;
    private UserSource source = UserSource.SOURCE_WEBSITE;
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

    public UserBuilder withSource(UserSource source) {
        this.source = source;
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
        user.setSource(source);
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
        return DigestUtils.sha1Hex(salt.concat(String.valueOf(random)));
    }
}