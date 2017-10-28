package com.darksci.kafka.webview.ui.manager.socket;

import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumer;
import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import com.darksci.kafka.webview.ui.manager.kafka.dto.KafkaResults;
import com.darksci.kafka.webview.ui.model.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketConsumersManager implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(WebSocketConsumersManager.class);

    /**
     * Holds consumers.
     */
    private final Map<ConsumerKey, ConsumerEntry> consumers = new ConcurrentHashMap<>();

    /**
     * For creating new consumers.
     */
    private final WebKafkaConsumerFactory webKafkaConsumerFactory;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketConsumersManager(final WebKafkaConsumerFactory webKafkaConsumerFactory, final SimpMessagingTemplate messagingTemplate) {
        this.webKafkaConsumerFactory = webKafkaConsumerFactory;
        this.simpMessagingTemplate = messagingTemplate;
    }

    public void addNewConsumer(final View view, final long userId, final String username) {
        synchronized (consumers) {
            // create a key
            final ConsumerKey consumerKey = new ConsumerKey(view.getId(), userId);

            if (consumers.containsKey(consumerKey)) {
                // TODO handle better
                throw new RuntimeException("Consumer already exists!");
            }

            final WebKafkaConsumer webKafkaConsumer = webKafkaConsumerFactory.create(view, new ArrayList<>(), userId);

            final ConsumerEntry consumerEntry = new ConsumerEntry(view.getId(), userId, username, webKafkaConsumer);
            consumers.put(consumerKey, consumerEntry);
        }
    }

    public void removeConsumersForUser(final long userId) {
        synchronized (consumers) {
            for (final Map.Entry<ConsumerKey, ConsumerEntry> entry : consumers.entrySet()) {
                if (entry.getKey().getUserId() != userId) {
                    continue;
                }
                entry.getValue().requestStop();
            }
        }
    }

    public void removeConsumerForUserAndView(final long viewId, final long userId) {
        synchronized (consumers) {
            // create a key
            final ConsumerKey consumerKey = new ConsumerKey(viewId, userId);
            if (!consumers.containsKey(consumerKey)) {
                return;
            }

            // Get entry
            final ConsumerEntry consumerEntry = consumers.get(consumerKey);

            // Close consumer
            consumerEntry.requestStop();
        }
    }

    @Override
    public void run() {
        // Loop thru consumers, consume, and publish to socket.
        do {
            final List<ConsumerKey> consumerKeysToRemove = new ArrayList<>();

            for (final Map.Entry<ConsumerKey, ConsumerEntry> entry : consumers.entrySet()) {
                try {
                    final ConsumerKey consumerKey = entry.getKey();
                    final ConsumerEntry consumerEntry = entry.getValue();

                    if (consumerEntry.isShouldStop()) {
                        // Close consumer
                        consumerEntry.getWebKafkaConsumer().close();

                        // Add to remove list
                        consumerKeysToRemove.add(consumerKey);
                        continue;
                    }

                    // Consume
                    final KafkaResults kafkaResults = consumerEntry.getWebKafkaConsumer().consumePerPartition();

                    // publish
                    final String username = consumerEntry.getUsername();
                    final String target = "/topic/view/" + consumerEntry.getViewId() + "/" + consumerEntry.getUserId();
                    simpMessagingTemplate.convertAndSendToUser(username, target, kafkaResults);
                } catch (final Exception exception) {
                    // Handle
                    logger.error(exception.getMessage(), exception);
                }
            }

            // Remove any consumers
            for (final ConsumerKey consumerKey: consumerKeysToRemove) {
                consumers.remove(consumerKey);
            }

            // Throttle with sleep
            try {
                Thread.sleep(5000L);
            } catch (final InterruptedException e) {
                break;
            }
        } while(true);
    }

    private static class ConsumerEntry {
        private final long userId;
        private final String username;
        private final long viewId;
        private final WebKafkaConsumer webKafkaConsumer;

        private boolean shouldStop = false;

        public ConsumerEntry(final long viewId, final long userId, final String username, final WebKafkaConsumer webKafkaConsumer) {
            this.userId = userId;
            this.viewId = viewId;
            this.username = username;
            this.webKafkaConsumer = webKafkaConsumer;
        }

        public WebKafkaConsumer getWebKafkaConsumer() {
            return webKafkaConsumer;
        }

        public synchronized boolean isShouldStop() {
            return shouldStop;
        }

        public synchronized void requestStop() {
            this.shouldStop = true;
        }

        public String getUsername() {
            return username;
        }

        public long getUserId() {
            return userId;
        }

        public long getViewId() {
            return viewId;
        }
    }

    /**
     * Represents a unique consumer key.
     */
    private static class ConsumerKey {
        private final long viewId;
        private final long userId;

        public ConsumerKey(final long viewId, final long userId) {
            this.viewId = viewId;
            this.userId = userId;
        }

        public long getViewId() {
            return viewId;
        }

        public long getUserId() {
            return userId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ConsumerKey that = (ConsumerKey) o;

            if (viewId != that.viewId) return false;
            return userId == that.userId;
        }

        @Override
        public int hashCode() {
            int result = (int) (viewId ^ (viewId >>> 32));
            result = 31 * result + (int) (userId ^ (userId >>> 32));
            return result;
        }
    }
}
