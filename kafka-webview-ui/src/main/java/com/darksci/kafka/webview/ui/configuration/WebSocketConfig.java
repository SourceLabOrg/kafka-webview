package com.darksci.kafka.webview.ui.configuration;

import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import com.darksci.kafka.webview.ui.manager.socket.PresenceEventListener;
import com.darksci.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Where messages published from the server side are published to.
        // OR ... the prefix for where consumers subscribe.
        config.enableSimpleBroker("/topic");

        //Controller end point prefixes, where consumers publish messages TO.
        config.setApplicationDestinationPrefixes("/websocket");
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry
            .addEndpoint("/websocket")
            .withSockJS();
    }

    @Bean
    @Description("Tracks user presence (join / leave) and shuts down consumers after a client disconnects.")
    public PresenceEventListener presenceEventListener(final WebSocketConsumersManager webSocketConsumersManager) {
        return new PresenceEventListener(webSocketConsumersManager);
    }

    @Bean
    public TaskExecutor backgroundConsumerExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Only a single thread in the pool
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("Background Consumer Thread(s)");
        executor.initialize();

        return executor;
    }

    /**
     * Manages kafka consumers running in a background processing thread for websocket consumers.
     */
    @Bean
    public WebSocketConsumersManager getWebSocketConsumersManager(
        final WebKafkaConsumerFactory webKafkaConsumerFactory,
        final SimpMessagingTemplate messagingTemplate,
        final TaskExecutor backgroundConsumerExecutor) {

        // Create manager
        final WebSocketConsumersManager manager = new WebSocketConsumersManager(webKafkaConsumerFactory, messagingTemplate);

        // Submit to executor service
        backgroundConsumerExecutor.execute(manager);

        return manager;
    }

}
