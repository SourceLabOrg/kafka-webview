package com.darksci.kafka.webview.ui.controller.login.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents the Lost Password Form.
 */
public class LostPasswordForm {
    @NotNull(message = "Enter your Email address")
    @Size(min = 2, max = 255, message = "Enter your Email address")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
