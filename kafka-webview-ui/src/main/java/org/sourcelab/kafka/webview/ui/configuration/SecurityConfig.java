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

package org.sourcelab.kafka.webview.ui.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.user.AnonymousUserDetailsService;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetails;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetailsService;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.request.RequestContextListener;

import java.util.ArrayList;

/**
 * Manages Security Configuration.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserRepository userRepository;
    private final AppProperties appProperties;

    public SecurityConfig(final UserRepository userRepository, final AppProperties appProperties) {
        this.userRepository = userRepository;
        this.appProperties = appProperties;
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security filter chain for actuator endpoints, secured using http basic auth.
     */
    @Bean
    @Order(1000)
    public SecurityFilterChain actuatorSecurityFilterChain(final HttpSecurity http) throws Exception {
        logger.info("Configuring Actuator access.");

        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/actuator/info", "/actuator/health", "/actuator/prometheus")
                    .permitAll()
                .anyRequest()
                    .hasRole("ADMIN"))
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Security filter chain for the web application.
     */
    @Bean
    @Order(1001)
    public SecurityFilterChain appSecurityFilterChain(final HttpSecurity http) throws Exception {
        // CSRF Enabled
        http.csrf(Customizer.withDefaults());

        // If user auth is enabled
        if (appProperties.isUserAuthEnabled()) {
            enableUserAuth(http);
        } else {
            disableUserAuth(http);
        }

        return http.build();
    }

    /**
     * Defines how users are loaded during authentication.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        if (appProperties.isUserAuthEnabled()) {
            logger.info("Configuring with locally authenticated user access");
            return new CustomUserDetailsService(userRepository);
        }
        return new AnonymousUserDetailsService();
    }

    /**
     * Sets up HttpSecurity for standard local user authentication.
     */
    private void enableUserAuth(final HttpSecurity http) throws Exception {
        logger.info("Configuring with authenticated user access.");

        http
            .authorizeHttpRequests(auth -> auth
                // Paths to static resources are available to anyone
                .requestMatchers("/register/**", "/login/**", "/vendors/**", "/css/**", "/js/**", "/img/**")
                    .permitAll()
                // Users can edit their own profile
                .requestMatchers("/configuration/user/edit/**", "/configuration/user/update")
                    .fullyAuthenticated()
                // Define admin only paths
                .requestMatchers(
                    // Configuration
                    "/configuration/**",

                    // Create topic
                    "/api/cluster/*/create/**",

                    // Modify topic
                    "/api/cluster/*/modify/**",

                    // Delete topic
                    "/api/cluster/*/delete/**",

                    // Remove consumer group
                    "/api/cluster/*/consumer/remove"

                ).hasRole("ADMIN")

                // All other requests must be authenticated
                .anyRequest()
                    .fullyAuthenticated())

            // Define how you login
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .failureUrl("/login?error=true")
                .defaultSuccessUrl("/")
                .permitAll())

            // And how you logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .permitAll());
    }

    /**
     * Sets up HttpSecurity for anonymous user access.
     */
    private void disableUserAuth(final HttpSecurity http) throws Exception {
        logger.info("Configuring with anonymous user access.");

        // Define the "User" that anonymous web clients will assume.
        final CustomUserDetails customUserDetails = AnonymousUserDetailsService.getDefaultAnonymousUser();

        http
            // All requests should require authorization as anonymous
            .authorizeHttpRequests(auth -> auth
                .anyRequest()
                .anonymous())
            // And the user provider should always return our anonymous user instance
            // with admin credentials.
            .anonymous(anonymous -> anonymous
                .principal(customUserDetails)
                .authorities(new ArrayList<>(customUserDetails.getAuthorities())));
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
