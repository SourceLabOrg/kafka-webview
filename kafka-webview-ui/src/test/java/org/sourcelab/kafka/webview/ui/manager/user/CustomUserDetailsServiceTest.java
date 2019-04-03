package org.sourcelab.kafka.webview.ui.manager.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests over CustomUserDetailsService.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CustomUserDetailsServiceTest {

    @Autowired
    private UserTestTools userTestTools;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleManager roleManager;

    /**
     * Verifies we can load a user correctly via the service.
     */
    @Test
    @Transactional
    public void testLoadUserByUsername() {
        // Define some permissions.
        final Permissions[] permissions = {
            Permissions.TOPIC_DELETE,
            Permissions.TOPIC_CREATE,
            Permissions.VIEW_CREATE
        };

        final ArrayList<String> expectedAuthorities = new ArrayList<>();
        expectedAuthorities.add("PERM_TOPIC_DELETE");
        expectedAuthorities.add("PERM_TOPIC_CREATE");
        expectedAuthorities.add("PERM_VIEW_CREATE");
        expectedAuthorities.add("ROLE_USER");

        // Create a user
        final User myUser = userTestTools.createUserWithPermissions(permissions);

        // Create instance.
        final CustomUserDetailsService service = new CustomUserDetailsService(userRepository, roleManager);

        // Call method under test.
        final CustomUserDetails userDetails = (CustomUserDetails) service.loadUserByUsername(myUser.getEmail());

        // Verify
        final Collection<? extends GrantedAuthority> authorityCollection = userDetails.getAuthorities();
        assertNotNull("Should not be null", authorityCollection);
        assertFalse("Should not be empty", authorityCollection.isEmpty());
        assertEquals(4, authorityCollection.size());

        final long foundEntries = authorityCollection
            .stream()
            .filter((authority) -> expectedAuthorities.contains(authority.getAuthority()))
            .count();

        assertEquals("Should have found all entries", expectedAuthorities.size(), foundEntries);

        // Verify other methods.
        assertEquals(myUser.getPassword(), userDetails.getPassword());
        assertEquals(myUser.getEmail(), userDetails.getUsername());
        assertEquals(myUser.getId(), userDetails.getUserId());
    }

    /**
     * Verifies we can load a user correctly via the service.
     */
    @Test(expected = UsernameNotFoundException.class)
    @Transactional
    public void testLoadUserByUsername_cannotFindUser() {
        // Create instance.
        final CustomUserDetailsService service = new CustomUserDetailsService(userRepository, roleManager);

        // Call method under test with invalid username, should toss exception.
        service.loadUserByUsername("Invalid Email");
    }
}