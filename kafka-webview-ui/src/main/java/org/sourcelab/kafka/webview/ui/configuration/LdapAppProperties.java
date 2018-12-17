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

package org.sourcelab.kafka.webview.ui.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * User defined LDAP authentication properties from the application configuration file.
 */
@Component
public class LdapAppProperties {
    /**
     * Global enable/disable toggle for LDAP auth.
     */
    @Value("${app.user.ldap.enabled:false}")
    private boolean enabled = false;

    /**
     * User DN lookup pattern.
     */
    @Value("${app.user.ldap.userDnPattern:}")
    private String userDnPattern;

    /**
     * Group search base.
     */
    @Value("${app.user.ldap.groupSearchBase:}")
    private String groupSearchBase;

    @Value("${app.user.ldap.groupRoleAttribute:}")
    private String groupRoleAttribute;

    /**
     * Ldap server url.
     */
    @Value("${app.user.ldap.url:}")
    private String url;

    /**
     * Password attribute.
     */
    @Value("${app.user.ldap.passwordAttribute:}")
    private String passwordAttribute;

    /**
     * Password encoder class.
     */
    @Value("${app.user.ldap.passwordEncoderClass:org.springframework.security.crypto.password.LdapShaPasswordEncoder}")
    private String passwordEncoderClass;

    /**
     * List of ldap groups that if a user is a member of they will be granted administrator access.
     */
    @Value("${app.user.ldap.adminGroups:}")
    private String[] adminGroups;

    /**
     * List of ldap groups that if a user is a member of they will be granted user access.
     */
    @Value("${app.user.ldap.userGroups:}")
    private String[] userGroups;

    /**
     * If LDAP server requires a username/password to connect with, provide it.
     */
    @Value("${app.user.ldap.bindUser}")
    private String bindUser = null;

    @Value("${app.user.ldap.bindUserPassword}")
    private String bindUserPassword = null;

    public boolean isEnabled() {
        return enabled;
    }

    public String getUserDnPattern() {
        return userDnPattern;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public String getUrl() {
        return url;
    }

    public String getPasswordAttribute() {
        return passwordAttribute;
    }

    public String getPasswordEncoderClass() {
        return passwordEncoderClass;
    }

    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }

    public String getBindUser() {
        return bindUser;
    }

    public String getBindUserPassword() {
        return bindUserPassword;
    }

    /**
     * Return immutable collection of all LDAP user groups that should be granted admin level access to app.
     * @return Immutable collection of user group names.
     */
    public Collection<String> getAdminGroups() {
        return Collections.unmodifiableCollection(
            Arrays.asList(adminGroups)
        );
    }

    /**
     * Return immutable collection of all LDAP user groups that should be granted user level access to app.
     * @return Immutable collection of user group names.
     */
    public Collection<String> getUserGroups() {
        return Collections.unmodifiableCollection(
            Arrays.asList(userGroups)
        );
    }

    @Override
    public String toString() {
        return "LdapAppProperties{"
            + "enabled=" + enabled
            + ", userDnPattern='" + userDnPattern + '\''
            + ", groupSearchBase='" + groupSearchBase + '\''
            + ", groupRoleAttribute='" + groupRoleAttribute + '\''
            + ", url='" + url + '\''
            + ", passwordAttribute='" + passwordAttribute + '\''
            + ", passwordEncoderClass='" + passwordEncoderClass + '\''
            + ", adminGroups=" + Arrays.toString(adminGroups)
            + ", userGroups=" + Arrays.toString(userGroups)
            + ", bindUser='" + bindUser + '\''
            + ", bindUserPassword='XXXXXX'"
            + '}';
    }
}
