package com.darksci.kafka.webview.ui.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Application configuration for Web resources.
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // css resource
        registry
            .addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/");

        // js resource
        registry
            .addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/");

        // js resource
        registry
            .addResourceHandler("/vendors/**")
            .addResourceLocations("classpath:/static/vendors/");

        // img resource
        registry
            .addResourceHandler("/img/**")
            .addResourceLocations("classpath:/static/img/");
    }
}