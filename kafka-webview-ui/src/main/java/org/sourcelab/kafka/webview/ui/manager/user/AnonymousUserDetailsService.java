package org.sourcelab.kafka.webview.ui.manager.user;

import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AnonymousUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final User anonymousUser = new User();
        anonymousUser.setId(0);
        anonymousUser.setDisplayName("Anonymous User");
        anonymousUser.setEmail("no-one");
        anonymousUser.setRole(UserRole.ROLE_ADMIN);
        anonymousUser.setActive(true);

        return new CustomUserDetails(anonymousUser);
    }
}
