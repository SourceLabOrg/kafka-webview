/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Smoke test authentication using LDAP authentication.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(value = {"app.user.enabled=true", "app.user.ldap.enabled=true", "app.user.ldap.url=ldap://localhost:55555/dc=example,dc=com"})
@AutoConfigureMockMvc
public class ApplicationLdapAuthenticationTest extends AbstractLoginTest {

    /**
     * Start embedded LDAP server.
     */
    @Rule
    public EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
        .newInstance()
        .usingDomainDsn("dc=example,dc=com")
        .importingLdifs("test-server.ldif")
        .bindingToPort(55555)
        .build();

    @Override
    public Collection<ValidCredentialsTestCase> getValidCredentials() {
        final List<ValidCredentialsTestCase> testCases = new ArrayList<>();

        // Admin user
        testCases.add(
            new ValidCredentialsTestCase("ben", "benspassword", "ben", 0, Arrays.asList("ROLE_ADMIN", "ROLE_USER"))
        );

        // Normal user
        testCases.add(
            new ValidCredentialsTestCase("bob", "bobspassword", "bob", 0, Arrays.asList("ROLE_USER"))
        );

        return testCases;
    }

    @Override
    public Collection<InvalidCredentialsTestCase> getInvalidCredentials() {
        final List<InvalidCredentialsTestCase> testCases = new ArrayList<>();

        // Just invalid users
        testCases.add(
            new InvalidCredentialsTestCase("", "benspassword")
        );
        testCases.add(
            new InvalidCredentialsTestCase("ben", "notbenspassword")
        );

        // Valid user, just not part of an allowed group
        testCases.add(
            new InvalidCredentialsTestCase("noone", "benspassword")
        );

        return testCases;
    }

    /**
     * Smoke test you must login to the app.
     */
    @Test
    public void test_mustLoginToUseApp() throws Exception {
        // Verify must authenticate.
        validateMustLogin();
    }
}
