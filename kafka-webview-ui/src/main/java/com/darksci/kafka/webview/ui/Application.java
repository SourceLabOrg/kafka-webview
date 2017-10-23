package com.darksci.kafka.webview.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main entry point.
 */
@EnableJpaRepositories("com.darksci.kafka.webview.ui.repository")
@EntityScan("com.darksci.kafka.webview.ui.model")
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
