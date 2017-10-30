package com.darksci.kafka.webview.ui.manager.socket;

import com.darksci.kafka.webview.ui.manager.kafka.SocketKafkaConsumer;
import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumer;
import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import com.darksci.kafka.webview.ui.manager.kafka.dto.KafkaResult;
import com.darksci.kafka.webview.ui.manager.kafka.dto.KafkaResults;
import com.darksci.kafka.webview.ui.model.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    /**
     * For sending responses to connected client.
     */
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final ThreadPoolExecutor threadPoolExecutor;



    public WebSocketConsumersManager(final WebKafkaConsumerFactory webKafkaConsumerFactory, final SimpMessagingTemplate messagingTemplate) {
        this.webKafkaConsumerFactory = webKafkaConsumerFactory;
        this.simpMessagingTemplate = messagingTemplate;

        // TODO evaluate how many threads.
        this.threadPoolExecutor = new ThreadPoolExecutor(
            100, 100, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>(100)
        );
    }

    public void addNewConsumer(final View view, final long userId, final String username) {
        synchronized (consumers) {
            // create a key
            final ConsumerKey consumerKey = new ConsumerKey(view.getId(), userId);

            if (consumers.containsKey(consumerKey)) {
                // TODO handle better
                throw new RuntimeException("Consumer already exists!");
            }

            // Create queue
            final Queue<KafkaResult> outputQueue = new LinkedBlockingQueue<>(256);

            // Create consumer
            final SocketKafkaConsumer webKafkaConsumer = webKafkaConsumerFactory.createWebSocketClient(
                view,
                new ArrayList<>(),
                userId,
                outputQueue
            );

            // Create entry
            final ConsumerEntry consumerEntry = new ConsumerEntry(view.getId(), userId, username, webKafkaConsumer);
            consumers.put(consumerKey, consumerEntry);

            // Toss into executor
            threadPoolExecutor.execute(webKafkaConsumer);
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
            boolean foundResult = false;
            final List<ConsumerKey> consumerKeysToRemove = new ArrayList<>();

            for (final Map.Entry<ConsumerKey, ConsumerEntry> entry : consumers.entrySet()) {

                try {
                    final ConsumerKey consumerKey = entry.getKey();
                    final ConsumerEntry consumerEntry = entry.getValue();

                    if (consumerEntry.isShouldStop()) {
                        // Add to remove list
                        consumerKeysToRemove.add(consumerKey);
                        continue;
                    }

                    // Consume
                    final KafkaResult kafkaResult = consumerEntry.nextResult();
                    if (kafkaResult == null) {
                        continue;
                    }
                    // Flip flag to true
                    foundResult = true;


                    // publish
                    final String username = consumerEntry.getUsername();
                    final String target = "/topic/view/" + consumerEntry.getViewId() + "/" + consumerEntry.getUserId();
                    simpMessagingTemplate.convertAndSendToUser(username, target, kafkaResult);
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
            if (!foundResult) {
                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException e) {
                    break;
                }
            }
        } while(true);

        // Shut down
        // TODO Handle shutdown.
        threadPoolExecutor.shutdown();
    }

    private static class ConsumerEntry {
        private final long userId;
        private final String username;
        private final long viewId;
        private final SocketKafkaConsumer socketKafkaConsumer;

        private boolean shouldStop = false;

        public ConsumerEntry(
            final long viewId,
            final long userId,
            final String username,
            final SocketKafkaConsumer socketKafkaConsumer) {
            this.userId = userId;
            this.viewId = viewId;
            this.username = username;
            this.socketKafkaConsumer = socketKafkaConsumer;
        }

        public KafkaResult nextResult() {
            return socketKafkaConsumer.nextResult();
        }

        public synchronized boolean isShouldStop() {
            return shouldStop;
        }

        public synchronized void requestStop() {
            this.socketKafkaConsumer.stop();
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
