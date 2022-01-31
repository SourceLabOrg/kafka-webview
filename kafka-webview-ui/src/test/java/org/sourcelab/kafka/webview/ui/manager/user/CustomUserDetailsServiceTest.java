/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CustomUserDetailsServiceTest {

    @Autowired
    private UserTestTools userTestTools;

    /**
     * Instance under test.
     */
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Verify what happens if you attempt to load a user which does not exist.
     */
    @Test(expected = UsernameNotFoundException.class)
    public void smokeTest_invalidUser() {
        final String email = "Does-not-exist@example.com";
        customUserDetailsService.loadUserByUsername(email);
    }

    /**
     * Test loading using the same case.
     */
    @Test
    public void smokeTest_loadValidUser_sameCase() {
        // Setup user.
        final String userEmail = "test" + System.currentTimeMillis() + "@example.com";
        final User user = userTestTools.createUser();
        user.setEmail(userEmail);
        userTestTools.save(user);

        // Attempt to load
        final UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

        // Verify
        assertNotNull("Result should be non-null", userDetails);
        assertTrue("Should be a CustomUserDetails instance", userDetails instanceof CustomUserDetails);
        assertEquals("Should have correct email", userEmail, userDetails.getUsername());
        assertEquals("Should have correct id",  user.getId(), ((CustomUserDetails) userDetails).getUserId());
        assertNotNull("Should have a user model", ((CustomUserDetails) userDetails).getUserModel());
    }

    /**
     * Test loading using insensitive case.
     */
    @Test
    public void smokeTest_loadValidUser_differentCasing() {
        // Setup user.
        final String userEmail = "test" + System.currentTimeMillis() + "@example.com";
        final User user = userTestTools.createUser();
        user.setEmail(userEmail);
        userTestTools.save(user);

        // Setup lookup email to have a different case.
        final String lookupEmail = userEmail.toUpperCase();

        // Attempt to load using different case.
        final UserDetails userDetails = customUserDetailsService.loadUserByUsername(lookupEmail);

        // Verify
        assertNotNull("Result should be non-null", userDetails);
        assertTrue("Should be a CustomUserDetails instance", userDetails instanceof CustomUserDetails);
        assertEquals("Should have correct email", userEmail, userDetails.getUsername());
        assertEquals("Should have correct id",  user.getId(), ((CustomUserDetails) userDetails).getUserId());
        assertNotNull("Should have a user model", ((CustomUserDetails) userDetails).getUserModel());
    }
}