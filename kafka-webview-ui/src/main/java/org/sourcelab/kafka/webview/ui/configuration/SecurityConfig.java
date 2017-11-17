package org.sourcelab.kafka.webview.ui.configuration;

import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetailsService;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.request.RequestContextListener;

/**
 * Manages Security Configuration.
 */
@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityProperties securityProperties;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            // CSRF Enabled
            .csrf().and()

            .authorizeRequests()
                // Paths to static resources are available to anyone
                .antMatchers("/register/**", "/login/**", "/vendors/**", "/css/**", "/js/**", "/img/**")
                    .permitAll()
                // Users can edit their own profile
                .antMatchers("/configuration/user/edit/**", "/configuration/user/update")
                    .fullyAuthenticated()
                // But other Configuration paths require ADMIN role.
                .antMatchers("/configuration/**")
                    .hasRole("ADMIN")
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
        
        // If require SSL is enabled
        if (securityProperties.isRequireSsl()) {
            // Ensure its enabled.
            http
                .requiresChannel()
                .anyRequest()
                .requiresSecure();
        }
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            // Define our custom user details service.
            .userDetailsService(new CustomUserDetailsService(userRepository))
            .passwordEncoder(getPasswordEncoder());
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
