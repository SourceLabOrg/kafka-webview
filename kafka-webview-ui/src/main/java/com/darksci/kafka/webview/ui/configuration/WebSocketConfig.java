package com.darksci.kafka.webview.ui.configuration;

import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import com.darksci.kafka.webview.ui.manager.socket.PresenceEventListener;
import com.darksci.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
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
    public WebSocketConsumersManager getWebSocketConsumersManager(final WebKafkaConsumerFactory webKafkaConsumerFactory, final SimpMessagingTemplate messagingTemplate) {
        return new WebSocketConsumersManager(webKafkaConsumerFactory, messagingTemplate);
    }

    @Bean
    @Description("Tracks user presence (join / leave) and broacasts it to all connected users")
    public PresenceEventListener presenceEventListener(final WebSocketConsumersManager webSocketConsumersManager) {
        return new PresenceEventListener(webSocketConsumersManager);
    }
}
