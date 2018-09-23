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

package org.sourcelab.kafka.webview.ui.configuration;

import org.sourcelab.kafka.webview.ui.manager.user.AnonymousUserDetailsService;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetails;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetailsService;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.request.RequestContextListener;

import java.util.ArrayList;

/**
 * Manages Security Configuration.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppProperties appProperties;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {

        // CSRF Enabled
        http
            .csrf();

        // If user auth is enabled
        if (appProperties.isUserAuthEnabled()) {
            // Set it up.
            enableUserAuth(http);
        } else {
            disableUserAuth(http);
        }
        
        // If require SSL is enabled
        if (appProperties.isRequireSsl()) {
            // Ensure its enabled.
            http
                .requiresChannel()
                .anyRequest()
                .requiresSecure();
        }
    }

    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {
        if (appProperties.isUserAuthEnabled()) {
            auth
                // Define our custom user details service.
                .userDetailsService(new CustomUserDetailsService(userRepository))
                .passwordEncoder(getPasswordEncoder());
        } else {
            auth
                // Define our custom user details service.
                .userDetailsService(new AnonymousUserDetailsService());
        }
    }

    /**
     * Sets up HttpSecurity for standard local user authentication.
     */
    private void enableUserAuth(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            // Paths to static resources are available to anyone
            .antMatchers("/register/**", "/login/**", "/vendors/**", "/css/**", "/js/**", "/img/**")
                .permitAll()
            // Users can edit their own profile
            .antMatchers("/configuration/user/edit/**", "/configuration/user/update")
                .fullyAuthenticated()
            // Define admin only paths
            .antMatchers(
                // Configuration
                "/configuration/**",

                // Create topic
                "/api/cluster/*/create/**",

                // Modify topic
                "/api/cluster/*/modify/**"
            ).hasRole("ADMIN")

            // All other requests must be authenticated
            .anyRequest()
                .fullyAuthenticated()
            .and()

            // Define how you login
            .formLogin()
            .loginPage("/login")
            .usernameParameter("email")
            .passwordParameter("password")
            .failureUrl("/login?error=true")
            .defaultSuccessUrl("/")
            .permitAll()
            .and()

            // And how you logout
            .logout()
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/login")
            .permitAll();
    }

    /**
     * Sets up HttpSecurity for standard local user authentication.
     */
    private void disableUserAuth(final HttpSecurity http) throws Exception {
        // Define the "User" that anonymous web clients will assume.
        final CustomUserDetails customUserDetails = AnonymousUserDetailsService.getDefaultAnonymousUser();

        http
            // All requests should require authorization as anonymous
            .authorizeRequests()
            .anyRequest()
            .anonymous()
            .and()
            // And the user provider should always return our anonymous user instance
            // with admin credentials.
            .anonymous()
            .principal(customUserDetails)
            .authorities(new ArrayList<>(customUserDetails.getAuthorities()));
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
