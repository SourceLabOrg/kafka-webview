package org.sourcelab.kafka.webview.ui.configuration;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manages WebServer Configuration and Customization.
 */
@Configuration
public class WebServerConfig {

    private static final String CONNECTOR_SCHEME = "http";
    private static final boolean CONNECTOR_SECURE = false;

    @Value("${app.secondary-port:9090}")
    private int secondaryPort;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return tomcatFactory -> {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setScheme(CONNECTOR_SCHEME);
            connector.setPort(secondaryPort);
            connector.setSecure(CONNECTOR_SECURE);
            tomcatFactory.addAdditionalTomcatConnectors(connector);
        };
    }

}
