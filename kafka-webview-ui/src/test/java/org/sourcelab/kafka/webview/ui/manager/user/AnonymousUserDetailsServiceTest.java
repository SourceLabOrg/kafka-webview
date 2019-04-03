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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AnonymousUserDetailsServiceTest {

    /**
     * Verifies we can load a user correctly via the service.
     */
    @Test
    @Transactional
    public void testLoadUserByUsername() {
        // Define expected permissions.
        final ArrayList<String> expectedAuthorities = new ArrayList<>();
        for (final Permissions permission : Permissions.values()) {
            expectedAuthorities.add("PERM_" + permission.name());
        }

        // Sanity test
        assertFalse("Sanity Test - Should not be empty", expectedAuthorities.isEmpty());

        // Add ROLE_USER authority.
        expectedAuthorities.add("ROLE_USER");

        // Create instance.
        final AnonymousUserDetailsService service = new AnonymousUserDetailsService();

        // Call method under test.
        final CustomUserDetails userDetails = (CustomUserDetails) service.loadUserByUsername("doesnt matter");

        // Verify
        final Collection<? extends GrantedAuthority> authorityCollection = userDetails.getAuthorities();
        assertNotNull("Should not be null", authorityCollection);
        assertFalse("Should not be empty", authorityCollection.isEmpty());

        // Should have number of permissions + 1.
        assertEquals(Permissions.values().length + 1, authorityCollection.size());

        final long foundEntries = authorityCollection
            .stream()
            .filter((authority) -> expectedAuthorities.contains(authority.getAuthority()))
            .count();

        assertEquals("Should have found all entries", expectedAuthorities.size(), foundEntries);

        // Verify other methods.
        assertNull(userDetails.getPassword());
        assertEquals("Anonymous User", userDetails.getUsername());
        assertEquals(0L, userDetails.getUserId());
        assertNotNull(userDetails.getUserModel());
    }
}