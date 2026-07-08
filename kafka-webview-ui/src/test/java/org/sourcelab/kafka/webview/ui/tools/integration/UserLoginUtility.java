/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.tools.integration;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class contains code for logging into the Kafka WebView app for integration tests against a "live" instance.
 */
public class UserLoginUtility {

    private final String hostname;
    private final String loginPath;
    private final TestRestTemplate restTemplate;

    /**
     * Constructor.
     * @param hostname Hostname and port for the service.  Example: "http://localhost:234234"
     */
    public UserLoginUtility(final String hostname, final TestRestTemplate testRestTemplate) {
        this(hostname, "/login", testRestTemplate);
    }

    /**
     * Constructor.
     * @param hostname Hostname and port for the service.  Example: "localhost:234234"
     * @param loginPath Path to login page, Example: "/login"
     * @param testRestTemplate RestTemplate to use.
     */
    public UserLoginUtility(final String hostname, final String loginPath, final TestRestTemplate testRestTemplate) {
        this.hostname = Objects.requireNonNull(hostname);
        this.loginPath = this.hostname + Objects.requireNonNull(loginPath);
        this.restTemplate = Objects.requireNonNull(testRestTemplate);
    }

    /**
     * Login to the instance with the given username and password.
     * @param user username to login with.
     * @param password Password to use.
     * @return http session headers.
     */
    public HttpHeaders login(final String user, final String password) {
        // Grab an anonymous session + csrf token from the login page.
        final HttpHeaders loginHeaders = getLoginHeaders();

        // Submit the login form without following the resulting redirect, so we can
        // capture the authenticated session cookie from the 302 response.
        final String formBody =
            "email=" + URLEncoder.encode(user, StandardCharsets.UTF_8)
            + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8)
            + "&_csrf=" + URLEncoder.encode(loginHeaders.getFirst("X-CSRF-TOKEN"), StandardCharsets.UTF_8);

        try (HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build()) {
            final HttpRequest request = HttpRequest.newBuilder(URI.create(loginPath))
                .header("Cookie", loginHeaders.getFirst("Cookie"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // A successful login redirects to the default success url "/".
            assertEquals("Login should respond with a redirect", 302, response.statusCode());
            final String location = response.headers().firstValue("Location").orElse("");
            assertTrue("Login should not redirect back to the login page: " + location, !location.contains("/login"));

            final String sessionCookie = response.headers()
                .firstValue("Set-Cookie")
                .map(UserLoginUtility::extractCookie)
                .orElseThrow(() -> new IllegalStateException("No session cookie returned from login."));

            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Cookie", sessionCookie);
            return httpHeaders;
        } catch (final java.io.IOException | InterruptedException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    /**
     * Strip cookie attributes (Path, HttpOnly, ...) from a Set-Cookie header value.
     * Tomcat 10 rejects Cookie headers that include them.
     */
    private static String extractCookie(final String setCookieValue) {
        return setCookieValue.split(";", 2)[0];
    }

    private HttpHeaders getLoginHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        final ResponseEntity<String> page = restTemplate.getForEntity(loginPath, String.class);

        // Should be 200 OK
        assertEquals(HttpStatus.OK, page.getStatusCode());

        final String cookie = extractCookie(page.getHeaders().getFirst("Set-Cookie"));
        headers.set("Cookie", cookie);
        final Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
        final Matcher matcher = pattern.matcher(page.getBody());
        assertTrue(matcher.matches());
        headers.set("X-CSRF-TOKEN", matcher.group(1));
        return headers;
    }
}
