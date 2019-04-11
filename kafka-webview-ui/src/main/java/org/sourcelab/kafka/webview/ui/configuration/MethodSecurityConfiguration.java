/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import org.sourcelab.kafka.webview.ui.manager.user.permission.PermissionAccessVoter;
import org.sourcelab.kafka.webview.ui.manager.user.permission.PermissionMetadataSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.method.DelegatingMethodSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * This configuration enables Method security configuration, enabling the use of
 * the @Secured and @PermissionRequired annotations.
 */
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    /**
     * This method enables scanning methods for the @PermissionRequired annotation to determine
     * which roles/permissions are required by the user calling.
     */
    @Override
    public MethodSecurityMetadataSource methodSecurityMetadataSource() {
        final DelegatingMethodSecurityMetadataSource source = (DelegatingMethodSecurityMetadataSource) super.methodSecurityMetadataSource();

        // Add our custom security metadata source implementation, which scans the @RequirePermission annotation.
        source
            .getMethodSecurityMetadataSources()
            .add(new PermissionMetadataSource());

        return source;
    }

    /**
     * This method appends a new DecisionVoter instance (PermissionAccessVoter) to the previously autoconfigured
     * list of Voters.
     *
     * This voter allows for validating required permissions set via the @RequirePermission annotation.
     */
    @Override
    protected AccessDecisionManager accessDecisionManager() {
        // Get originally configured voter manager instance.
        final AffirmativeBased originalVoter = (AffirmativeBased) super.accessDecisionManager();

        // Get original voters associated with it.
        final List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>(originalVoter.getDecisionVoters());

        // Append our custom voeter
        decisionVoters.add(new PermissionAccessVoter());

        // Create new instance including our new instance.
        return new AffirmativeBased(decisionVoters);
    }
}