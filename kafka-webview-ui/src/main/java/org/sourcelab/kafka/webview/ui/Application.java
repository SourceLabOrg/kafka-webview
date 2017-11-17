package org.sourcelab.kafka.webview.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main entry point.
 */
@EnableJpaRepositories("org.sourcelab.kafka.webview.ui.repository")
@EntityScan("org.sourcelab.kafka.webview.ui.model")
@SpringBootApplication
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
