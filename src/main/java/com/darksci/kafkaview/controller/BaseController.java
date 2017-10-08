package com.darksci.kafkaview.controller;

import com.darksci.kafkaview.configuration.CustomUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base Controller w/ common code.
 */
public abstract class BaseController {

    /**
     * Determine if the current user is logged in or not.
     * @return True if so, false if not.
     */
    protected boolean isLoggedIn() {
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
}
