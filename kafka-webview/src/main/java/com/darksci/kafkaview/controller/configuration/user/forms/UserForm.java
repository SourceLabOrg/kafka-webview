package com.darksci.kafkaview.controller.configuration.user.forms;

import com.darksci.kafkaview.model.UserRole;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserForm {
    private Long id = null;

    @NotNull(message = "Please enter an Email address")
    @Size(min = 2, max = 255)
    private String email;

    @NotNull(message = "Please enter a name")
    @Size(min = 2, max = 64)
    private String displayName;

    @Size(min = 8, max = 255, message = "Please enter a password of at least 8 characters")
    private String password;

    @Size(min = 8, max = 255)
    private String password2;

    @NotNull(message = "Select a user role")
    private UserRole userRole = UserRole.ROLE_USER;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(final String password2) {
        this.password2 = password2;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(final UserRole userRole) {
        this.userRole = userRole;
    }

    public boolean exists() {
        return getId() != null;
    }

    @Override
    public String toString() {
        return "UserForm{" +
            "id=" + id +
            ", email='" + email + '\'' +
            ", userRole=" + userRole +
            '}';
    }
}
