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

import org.sourcelab.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import org.sourcelab.kafka.webview.ui.manager.socket.PresenceEventListener;
import org.sourcelab.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
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

/**
 * Application Configuration for Web Sockets.
 */
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

    /**
     * This thread runs the WebSocketConsumerManager, which manages any consumers for web sockets.
     * It only needs a single thread, because the manager starts up its own managed thread pool.
     * @return new ThreadPool Task executor.
     */
    @Bean
    public TaskExecutor backgroundConsumerExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Only a single thread in the pool
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("Web Socket Consumer Manager");
        executor.initialize();

        return executor;
    }

    /**
     * Manages kafka consumers running in a background processing thread for websocket consumers.
     * @param webKafkaConsumerFactory Factory for creating new Consumers
     * @param messagingTemplate messaging template instance for passing websocket messages.
     * @param backgroundConsumerExecutor The executor to run our manager in.
     * @param appProperties defined app properties.
     * @return manager instance for web socket consumers.
     */
    @Bean
    public WebSocketConsumersManager getWebSocketConsumersManager(
        final WebKafkaConsumerFactory webKafkaConsumerFactory,
        final SimpMessagingTemplate messagingTemplate,
        final TaskExecutor backgroundConsumerExecutor,
        final AppProperties appProperties) {

        // Create manager
        final WebSocketConsumersManager manager = new WebSocketConsumersManager(
            webKafkaConsumerFactory,
            messagingTemplate,
            appProperties.getMaxConcurrentWebSocketConsumers()
        );

        // Submit to executor service
        backgroundConsumerExecutor.execute(manager);

        return manager;
    }
}