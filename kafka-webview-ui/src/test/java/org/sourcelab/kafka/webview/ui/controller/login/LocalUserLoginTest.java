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

package org.sourcelab.kafka.webview.ui.controller.login;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.tools.UserTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Verifies user login using locally defined users from the database.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(value = {"app.user.enabled=true", "app.user.ldap.enabled=false"})
@AutoConfigureMockMvc
public class LocalUserLoginTest extends AbstractLoginTest {

    @Autowired
    protected UserTestTools userTestTools;

    @Override
    public Collection<ValidCredentialsTestCase> getValidCredentials() {
        // Create two users
        final User adminUser = userTestTools.createAdminUser();
        final User user = userTestTools.createUser();

        final List<ValidCredentialsTestCase> testCases = new ArrayList<>();

        // Admin user
        testCases.add(
            new ValidCredentialsTestCase(adminUser.getEmail(), "RandomPassword", adminUser.getEmail(), adminUser.getId(), Arrays.asList("ROLE_ADMIN", "ROLE_USER"))
        );

        // Normal user
        testCases.add(
            new ValidCredentialsTestCase(user.getEmail(), "RandomPassword", user.getEmail(), user.getId(), Arrays.asList("ROLE_USER"))
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
