package com.darksci.kafkaview.controller;

import com.darksci.kafkaview.manager.user.CustomUserDetails;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.model.View;
import com.darksci.kafkaview.repository.ClusterRepository;
import com.darksci.kafkaview.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

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
     * This gets executed for all requests.
     */
    @ModelAttribute
    public void addAttributes(Model model) {
        // TODO put a limit on these
        final Iterable<Cluster> clusters = clusterRepository.findAllByOrderByNameAsc();
        final Iterable<View> views = viewRepository.findAllByOrderByNameAsc();

        model.addAttribute("MenuClusters", clusters);
        model.addAttribute("MenuViews", views);
    }

}
