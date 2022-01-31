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

package org.sourcelab.kafka.webview.ui.tools.integration;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
        final HttpHeaders httpHeaders = new HttpHeaders();

        restTemplate.execute(loginPath, HttpMethod.POST,
            request -> {
                request.getHeaders().addAll(getLoginHeaders());
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("email", user);
                map.add("password", password);
                new FormHttpMessageConverter().write(map, MediaType.APPLICATION_FORM_URLENCODED, request);
            },
            response -> {
                httpHeaders.add("Cookie", response.getHeaders().getFirst("Set-Cookie"));
                return null;
            });

        return httpHeaders;
    }

    private HttpHeaders getLoginHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        final ResponseEntity<String> page = restTemplate.getForEntity(loginPath, String.class);

        // Should be 200 OK
        assertEquals(HttpStatus.OK, page.getStatusCode());

        final String cookie = page.getHeaders().getFirst("Set-Cookie");
        headers.set("Cookie", cookie);
        final Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
        final Matcher matcher = pattern.matcher(page.getBody());
        assertTrue(matcher.matches());
        headers.set("X-CSRF-TOKEN", matcher.group(1));
        return headers;
    }
}
