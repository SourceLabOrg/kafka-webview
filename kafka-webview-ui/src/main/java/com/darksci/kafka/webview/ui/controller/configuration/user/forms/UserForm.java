package com.darksci.kafka.webview.ui.controller.configuration.user.forms;

import com.darksci.kafka.webview.ui.model.UserRole;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents the User create/update form.
 */
public class UserForm {
    private Long id = null;

    @NotNull(message = "Please enter an Email address")
    @Size(min = 2, max = 255)
    private String email;

    @NotNull(message = "Please enter a name")
    @Size(min = 2, max = 64)
    private String displayName;

    private String password;
    
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

    /**
     * Does the User represented on the form already exist in the database.
     */
    public boolean exists() {
        return getId() != null;
    }

    @Override
    public String toString() {
        return "UserForm{"
            + "id=" + id
            + ", email='" + email + '\''
            + ", displayName='" + displayName + '\''
            + ", password='" + password + '\''
            + ", password2='" + password2 + '\''
            + ", userRole=" + userRole
            + '}';
    }
}
