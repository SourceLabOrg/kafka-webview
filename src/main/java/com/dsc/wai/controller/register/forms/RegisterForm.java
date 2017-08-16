package com.dsc.wai.controller.register.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents the registration form.
 */
public class RegisterForm {
    @NotNull(message = "Please enter an Email address")
    @Size(min = 2, max = 255)
    private String email;

    @NotNull(message = "Please enter a name")
    @Size(min = 2, max = 64)
    private String displayName;

    @NotNull(message = "Please enter a password")
    @Size(min = 7, max = 128, message = "Please enter a password of at least 7 characters")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @NotNull(message = "Please enter a display name")
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

    @Override
    public String toString() {
        return "RegisterForm{"
            + "email='" + email + '\''
            + ", displayName='" + displayName + '\''
            + ", password='(" + password.length() + ")'"
            + '}';
    }
}
