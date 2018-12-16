package org.sourcelab.kafka.webview.ui.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;

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
    @Value("${app.user.ldap.userDnPattern}")
    private String userDnPattern;

    /**
     * Group search base.
     */
    @Value("${app.user.ldap.groupSearchBase}")
    private String groupSearchBase;

    @Value("${app.user.ldap.groupRoleAttribute}")
    private String groupRoleAttribute;

    /**
     * Ldap server url.
     */
    @Value("${app.user.ldap.url}")
    private String url;

    /**
     * Password attribute.
     */
    @Value("${app.user.ldap.passwordAttribute}")
    private String passwordAttribute;

    /**
     * Password encoder class.
     */
    @Value("${app.user.ldap.passwordEncoderClass}")
    private String passwordEncoderClass;

    @Value("${app.user.ldap.adminGroups}")
    private String[] adminGroups;

    @Value("${app.user.ldap.userGroups}")
    private String[] userGroups;

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

    public Collection<String> getAdminGroups() {
        return Arrays.asList(adminGroups);
    }

    public Collection<String> getUserGroups() {
        return Arrays.asList(userGroups);
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
            + ", adminGroups=" + adminGroups
            + ", userGroups=" + userGroups
            + '}';
    }
}
