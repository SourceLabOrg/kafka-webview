package com.darksci.kafka.webview.ui.controller;

import com.darksci.kafka.webview.ui.manager.user.CustomUserDetails;
import com.darksci.kafka.webview.ui.model.Cluster;
import com.darksci.kafka.webview.ui.model.View;
import com.darksci.kafka.webview.ui.repository.ClusterRepository;
import com.darksci.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /**
     * Determine if the current user is logged in or not.
     * @return True if so, false if not.
     */
    protected boolean isLoggedIn() {
        // For now bypass auth
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
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
