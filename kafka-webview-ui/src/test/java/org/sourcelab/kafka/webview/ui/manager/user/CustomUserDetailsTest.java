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

package org.sourcelab.kafka.webview.ui.manager.user;

import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Sanity validation of CustomUserDetails.
 */
public class CustomUserDetailsTest {

    /**
     * Test getAuthorities returns the expected values.
     */
    @Test
    public void testGetAuthorities() {
        // Create model
        final User myUser = new User();

        // Define permissions
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

        // Create instance.
        final CustomUserDetails userDetails = new CustomUserDetails(myUser, Arrays.asList(permissions));

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
    }

    /**
     * Test various getters on instance.
     */
    @Test
    public void testGetters() {
        // Create model
        final User myUser = new User();
        myUser.setId(123123L);
        myUser.setPassword("MyPassword");
        myUser.setEmail("myEmail@email.com");
        myUser.setActive(true);

        // Define permissions
        final Permissions[] permissions = {
            Permissions.TOPIC_DELETE,
            Permissions.TOPIC_CREATE,
            Permissions.VIEW_CREATE
        };

        // Create instance.
        final CustomUserDetails userDetails = new CustomUserDetails(myUser, Arrays.asList(permissions));

        // Verify
        assertEquals("MyPassword", userDetails.getPassword());
        assertEquals("myEmail@email.com", userDetails.getUsername());
        assertEquals(123123L, userDetails.getUserId());
        assertEquals(myUser, userDetails.getUserModel());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());

        // Update user model set not active
        myUser.setActive(false);

        // Create new instance.
        final CustomUserDetails userDetailsInactive = new CustomUserDetails(myUser, Arrays.asList(permissions));

        // Verify
        assertFalse(userDetailsInactive.isEnabled());
        assertFalse(userDetailsInactive.isAccountNonExpired());
        assertFalse(userDetailsInactive.isAccountNonLocked());
        assertFalse(userDetailsInactive.isCredentialsNonExpired());
    }
}