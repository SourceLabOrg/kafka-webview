package com.darksci.kafkaview.controller.login.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class LoginForm {
    @NotNull(message = "Enter your Email address")
    @Size(min = 2, max = 255)
    private String email;

    @NotNull(message = "Enter your password")
    @Size(min = 7, max = 128, message = "Enter your password")
    private String password;

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
}
