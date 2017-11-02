package com.darksci.kafka.webview.ui.manager.socket;

import com.darksci.kafka.webview.ui.manager.kafka.SessionIdentifier;
import com.darksci.kafka.webview.ui.manager.kafka.SocketKafkaConsumer;
import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import com.darksci.kafka.webview.ui.manager.kafka.dto.KafkaResult;
import com.darksci.kafka.webview.ui.model.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
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

    /**
     * Threadpool we run consumers within.
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    public WebSocketConsumersManager(
        final WebKafkaConsumerFactory webKafkaConsumerFactory,
        final SimpMessagingTemplate messagingTemplate,
        final int maxConcurrentConsumers) {
        this.webKafkaConsumerFactory = webKafkaConsumerFactory;
        this.simpMessagingTemplate = messagingTemplate;

        // Setup managed thread pool with number of concurrent threads.
        // TODO add handler for when a new connection comes in that exceeds the maximum running concurrent consumers.
        this.threadPoolExecutor = new ThreadPoolExecutor(
            maxConcurrentConsumers, maxConcurrentConsumers, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>(100)
        );
    }

    public void addNewConsumer(final View view, final SessionIdentifier sessionIdentifier) {
        synchronized (consumers) {
            // createWebClient a key
            final ConsumerKey consumerKey = new ConsumerKey(view.getId(), sessionIdentifier);

            if (consumers.containsKey(consumerKey)) {
                // TODO handle better
                throw new RuntimeException("Consumer already exists!");
            }

            // Create consumer
            final SocketKafkaConsumer webKafkaConsumer = webKafkaConsumerFactory.createWebSocketClient(
                view,
                new ArrayList<>(),
                sessionIdentifier
            );

            // Create entry
            final ConsumerEntry consumerEntry = new ConsumerEntry(webKafkaConsumer);
            consumers.put(consumerKey, consumerEntry);

            // Toss into executor
            threadPoolExecutor.execute(webKafkaConsumer);

            // Add logger statement
            logger.info("Added new web socket consumer, now has {}/{} running consumers",
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getMaximumPoolSize()
            );
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

    public void removeConsumerForUserAndView(final long viewId, final SessionIdentifier sessionIdentifier) {
        synchronized (consumers) {
            // createWebClient a key
            final ConsumerKey consumerKey = new ConsumerKey(viewId, sessionIdentifier);
            if (!consumers.containsKey(consumerKey)) {
                return;
            }

            // Get entry
            final ConsumerEntry consumerEntry = consumers.get(consumerKey);

            // Close consumer
            consumerEntry.requestStop();
        }
    }

    public void pauseConsumer(final long viewId, final SessionIdentifier sessionIdentifier) {
        synchronized (consumers) {
            // createWebClient a key
            final ConsumerKey consumerKey = new ConsumerKey(viewId, sessionIdentifier);
            if (!consumers.containsKey(consumerKey)) {
                return;
            }

            // Get entry
            final ConsumerEntry consumerEntry = consumers.get(consumerKey);

            // Lets pause it
            consumerEntry.requestPause();
        }
    }

    public void resumeConsumer(final long viewId, final SessionIdentifier sessionIdentifier) {
        synchronized (consumers) {
            // createWebClient a key
            final ConsumerKey consumerKey = new ConsumerKey(viewId, sessionIdentifier);
            if (!consumers.containsKey(consumerKey)) {
                return;
            }

            // Get entry
            final ConsumerEntry consumerEntry = consumers.get(consumerKey);

            // Lets pause it
            consumerEntry.requestResume();
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
                    final Optional<KafkaResult> kafkaResult = consumerEntry.nextResult();
                    if (!kafkaResult.isPresent()) {
                        continue;
                    }
                    // Flip flag to true
                    foundResult = true;


                    // publish
                    final String target = "/topic/view/" + consumerKey.getViewId() + "/" + consumerKey.getUserId();

                    final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
                    headerAccessor.setSessionId(consumerKey.getSessionId());
                    headerAccessor.setLeaveMutable(true);

                    // Only send it to the specific user's sesison Id.
                    simpMessagingTemplate.convertAndSendToUser(
                        consumerKey.getSessionId(),
                        target,
                        kafkaResult.get(),
                        headerAccessor.getMessageHeaders()
                    );
                } catch (final Exception exception) {
                    // Handle
                    logger.error(exception.getMessage(), exception);
                }
            }

            // Remove any consumers
            for (final ConsumerKey consumerKey: consumerKeysToRemove) {
                consumers.remove(consumerKey);

                // Add logger statement
                logger.info("Removed web socket consumer, now has {}/{} running consumers",
                    threadPoolExecutor.getActiveCount(),
                    threadPoolExecutor.getMaximumPoolSize()
                );
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
        private final SocketKafkaConsumer socketKafkaConsumer;

        /**
         * Flag if we should requestStop.
         */
        private boolean shouldStop = false;

        /**
         * Flag if we should be paused.
         */
        private boolean isPaused = false;

        public ConsumerEntry(final SocketKafkaConsumer socketKafkaConsumer) {
            this.socketKafkaConsumer = socketKafkaConsumer;
        }

        public Optional<KafkaResult> nextResult() {
            // If paused
            if (isPaused) {
                // always return false.  This will cause the internal buffer to block
                // on the consumer side.
                return Optional.empty();
            }
            return socketKafkaConsumer.nextResult();
        }

        public synchronized boolean isShouldStop() {
            return shouldStop;
        }

        public synchronized void requestStop() {
            this.socketKafkaConsumer.requestStop();
            this.shouldStop = true;
        }

        public synchronized void requestPause() {
            this.isPaused = true;
        }

        public synchronized void requestResume() {
            this.isPaused = false;
        }
    }

    /**
     * Represents a unique consumer key.
     */
    private static class ConsumerKey {
        private final long viewId;
        private final long userId;
        private final String sessionId;

        public ConsumerKey(final long viewId, final SessionIdentifier sessionIdentifier) {
            this.viewId = viewId;
            this.userId = sessionIdentifier.getUserId();
            this.sessionId = sessionIdentifier.getSessionId();
        }

        public long getViewId() {
            return viewId;
        }

        public long getUserId() {
            return userId;
        }

        public String getSessionId() {
            return sessionId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ConsumerKey that = (ConsumerKey) o;

            if (viewId != that.viewId) return false;
            if (userId != that.userId) return false;
            return sessionId.equals(that.sessionId);
        }

        @Override
        public int hashCode() {
            int result = (int) (viewId ^ (viewId >>> 32));
            result = 31 * result + (int) (userId ^ (userId >>> 32));
            result = 31 * result + sessionId.hashCode();
            return result;
        }
    }
}
