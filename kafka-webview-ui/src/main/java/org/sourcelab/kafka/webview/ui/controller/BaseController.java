/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.controller;

import org.sourcelab.kafka.webview.ui.configuration.AppProperties;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetails;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collection;

/**
 * Base Controller w/ common code.
 */
public abstract class BaseController {

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private AppProperties appProperties;

    /**
     * Determine if the current user is logged in or not.
     * @return True if so, false if not.
     */
    protected boolean isLoggedIn() {
        // If not authenticated.
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }

        // If anonymous user, but user auth is enabled
        if (auth instanceof AnonymousAuthenticationToken && appProperties.isUserAuthEnabled()) {
            // Then we are not authenticated.
            return false;
        }
        return true;
    }

    /**
     * @return Currently logged in user's details.
     */
    protected CustomUserDetails getLoggedInUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * @return Currently logged in user Id.
     */
    protected long getLoggedInUserId() {
        return getLoggedInUser().getUserId();
    }

    /**
     * @return Currently logged in user's session id.
     */
    protected String getLoggedInUserSessionId() {
        return ((WebAuthenticationDetails)SecurityContextHolder.getContext().getAuthentication().getDetails()).getSessionId();
    }

    /**
     * This gets executed for all requests.
     */
    @ModelAttribute
    public void addAttributes(Model model) {
        // But only if logged in
        if (!isLoggedIn()) {
            return;
        }

        // TODO put a limit on these
        final Iterable<Cluster> clusters = clusterRepository.findAllByOrderByNameAsc();
        final Iterable<View> views = viewRepository.findAllByOrderByNameAsc();

        model.addAttribute("MenuClusters", clusters);
        model.addAttribute("MenuViews", views);
        model.addAttribute("UserId", getLoggedInUserId());

        if (!appProperties.isUserAuthEnabled() || appProperties.getLdapProperties().isEnabled()) {
            model.addAttribute("MenuShowUserConfig", false);
        } else {
            model.addAttribute("MenuShowUserConfig", true);
        }
    }

    /**
     * Determine if the authentication has the requested role.
     * @param role The role to look for.
     * @return Boolean, true if so, false if not.
     */
    protected boolean hasRole(final String role) {
        final String realRole = "ROLE_" + role;
        final Collection<? extends GrantedAuthority> authorities = getLoggedInUser().getAuthorities();

        // Find
        for (final GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(realRole)) {
                return true;
            }
        }
        return false;
    }
}
