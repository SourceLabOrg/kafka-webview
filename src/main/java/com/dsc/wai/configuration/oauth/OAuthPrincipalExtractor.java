package com.dsc.wai.configuration.oauth;

import com.dsc.wai.configuration.CustomUserDetails;
import com.dsc.wai.manager.user.NewUserManager;
import com.dsc.wai.model.User;
import com.dsc.wai.model.UserSource;
import com.dsc.wai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;

import java.util.Map;

/**
 * Given the response from an OAuth Single Signon request, convert that to
 * the appropriate local user.  If the local user doesn't exist yet, create it!
 */
public class OAuthPrincipalExtractor implements PrincipalExtractor {
    private static final Logger logger = LoggerFactory.getLogger(OAuthPrincipalExtractor.class);

    private final UserRepository userRepository;
    private final UserSource userSource;
    private final NewUserManager newUserManager;

    public OAuthPrincipalExtractor(final UserRepository userRepository, final UserSource userSource, final NewUserManager newUserManager) {
        this.userRepository = userRepository;
        this.userSource = userSource;
        this.newUserManager = newUserManager;
    }

    @Override
    public Object extractPrincipal(final Map<String, Object> map) {
        logger.info("Map: {}", map);
        if (!map.containsKey("email")) {
            // Do something?
            return null;
        }

        // Grab email from oauth response
        final String email = (String) map.get("email");
        final String name = (String) map.get("name");

        // Retrieve user
        User user = userRepository.findByEmail(email);

        // If we have no user
        if (user == null) {
            // Create the user
            user = newUserManager.createNewOAuthUser(email, name, userSource);
        }

        // Create userDetails
        return new CustomUserDetails(user);
    }
}
