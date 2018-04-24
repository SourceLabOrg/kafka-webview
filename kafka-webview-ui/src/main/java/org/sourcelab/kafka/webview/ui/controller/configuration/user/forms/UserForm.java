/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.controller.configuration.user.forms;

import org.sourcelab.kafka.webview.ui.model.UserRole;

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
            + ", password='XXXXX'"
            + ", password2='XXXXX'"
            + ", userRole=" + userRole
            + '}';
    }
}
