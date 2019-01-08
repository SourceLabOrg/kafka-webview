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

package org.sourcelab.kafka.webview.ui.controller.login;

import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractLoginTest {
    @Autowired
    protected MockMvc mockMvc;

    /**
     * @return Collection of test cases for valid logins.
     */
    public abstract Collection<ValidCredentialsTestCase> getValidCredentials();

    /**
     * @return Collection of test cases for invalid logins.
     */
    public abstract Collection<InvalidCredentialsTestCase> getInvalidCredentials();

    /**
     * Attempt to login with invalid credentials.
     */
    @Test
    public void test_invalidLoginAuthentication() throws Exception {

        for (final InvalidCredentialsTestCase testCase : getInvalidCredentials()) {
            // Attempt to login now
            final MvcResult result = mockMvc
                .perform(post("/login")
                    .with(anonymous())
                    .with(csrf())
                    .param("email", testCase.getUsername())
                    .param("password", testCase.getPassword()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"))
                .andReturn();

            final MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
            assertNotNull(session);
            assertNull("Should have no security context", session.getValue("SPRING_SECURITY_CONTEXT"));
        }
    }

    /**
     * Attempt to authenticate using embedded LDAP server as an administrator user.
     */
    @Test
    public void test_validLoginAuthenticate() throws Exception {
        for (final ValidCredentialsTestCase testCase : getValidCredentials()) {
            // Attempt to login now
            final MvcResult result = mockMvc
                .perform(post("/login")
                    .with(anonymous())
                    .with(csrf())
                    .param("email", testCase.getUsername())
                    .param("password", testCase.getPassword()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

            // Validate session is valid
            validateAuthenticated(
                result,
                testCase.getExpectedUsername(),
                testCase.getExpectedUserId(),
                testCase.getExpectedRoles()
            );
        }
    }

    protected void validateAuthenticated(
        final MvcResult result,
        final String expectedUsername,
        final long expectedUserId,
        final Collection<String> expectedRoles
    ) {
        // Validate session is valid
        final MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        assertNotNull("Session should not be null", session);
        assertTrue("Session should be new", session.isNew());
        assertFalse("sesison should be valid", session.isInvalid());

        // Pull out context
        final SecurityContext securityContext = (SecurityContext) session.getValue("SPRING_SECURITY_CONTEXT");
        assertNotNull("Should be authenticated", securityContext);
        final UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) securityContext.getAuthentication();
        assertNotNull("Should be authenticated", authenticationToken);

        // Verify we have the correct roles
        expectedRoles.forEach((expectedRole) -> {
            assertTrue("Should have user role", authenticationToken.getAuthorities().contains(new SimpleGrantedAuthority(expectedRole)));
        });
        assertEquals("Should have no extra roles", expectedRoles.size(), authenticationToken.getAuthorities().size());

        final CustomUserDetails customUserDetails = (CustomUserDetails) authenticationToken.getPrincipal();
        expectedRoles.forEach((expectedRole) -> {
            assertTrue("Should have user role", customUserDetails.getAuthorities().contains(new SimpleGrantedAuthority(expectedRole)));
        });
        assertEquals("Should have no extra roles", expectedRoles.size(), customUserDetails.getAuthorities().size());

        assertEquals("LDAP Users should have userId", expectedUserId, customUserDetails.getUserId());
        assertEquals("Should have username", expectedUsername, customUserDetails.getUsername());
    }

    protected void validateMustLogin() throws Exception {
        // Hit the home page and we should get redirected to login page.
        final MvcResult result = mockMvc
            .perform(get("/")
                .with(anonymous()))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/login"))
            .andReturn();

        final MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        assertNull("Should have no security context", session.getValue("SPRING_SECURITY_CONTEXT"));
    }

    protected static class ValidCredentialsTestCase {
        private String username;
        private String password;
        private String expectedUsername;
        private long expectedUserId;
        private Collection<String> expectedRoles;

        public ValidCredentialsTestCase(final String username, final String password, final String expectedUsername, final long expectedUserId, final Collection<String> expectedRoles) {
            this.username = username;
            this.password = password;
            this.expectedUsername = expectedUsername;
            this.expectedUserId = expectedUserId;
            this.expectedRoles = expectedRoles;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getExpectedUsername() {
            return expectedUsername;
        }

        public long getExpectedUserId() {
            return expectedUserId;
        }

        public Collection<String> getExpectedRoles() {
            return expectedRoles;
        }
    }

    protected static class InvalidCredentialsTestCase {
        private final String username;
        private final String password;

        public InvalidCredentialsTestCase(final String username, final String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
