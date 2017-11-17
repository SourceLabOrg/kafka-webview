package org.sourcelab.kafka.webview.ui.controller.login.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents the reset password form.
 */
public class ResetPasswordForm {
    @NotNull(message = "Enter your Email address")
    @Size(min = 2, max = 255, message = "Enter your Email address")
    private String email;

    @NotNull(message = "Please enter a password")
    @Size(min = 7, max = 128, message = "Please enter a password of at least 7 characters")
    private String password;

    @NotNull(message = "Enter your reset token")
    private String token;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
