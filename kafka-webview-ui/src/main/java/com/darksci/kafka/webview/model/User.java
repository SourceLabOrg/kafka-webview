package com.darksci.kafka.webview.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * User model.
 */
@Entity(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "0")
    private UserRole role;

    @Column(nullable = true, name = "reset_password_hash")
    private String resetPasswordHash;

    @Column(nullable = false, name = "has_password")
    private Boolean hasPassword;

    @Column(nullable = false, name = "is_active")
    private Boolean isActive;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(final Boolean active) {
        isActive = active;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(final UserRole role) {
        this.role = role;
    }

    public String getResetPasswordHash() {
        return resetPasswordHash;
    }

    public void setResetPasswordHash(final String resetPasswordHash) {
        this.resetPasswordHash = resetPasswordHash;
    }

    public Boolean getHasPassword() {
        return hasPassword;
    }

    public void setHasPassword(final Boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", email='" + email + '\'' +
            ", displayName='" + displayName + '\'' +
            ", password='" + password + '\'' +
            ", role=" + role +
            ", resetPasswordHash='" + resetPasswordHash + '\'' +
            ", isActive=" + isActive +
            '}';
    }
}
