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

package org.sourcelab.kafka.webview.ui.manager.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.encryption.Sha1Tools;
import org.sourcelab.kafka.webview.ui.manager.kafka.SessionIdentifier;
import org.sourcelab.kafka.webview.ui.manager.kafka.SocketKafkaConsumer;
import org.sourcelab.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterDefinition;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResult;
import org.sourcelab.kafka.webview.ui.model.View;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Manages background kafka consumers and transfers consumed messages from them to their
 * corresponding WebSocket connections.
 */
public class WebSocketConsumersManager implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConsumersManager.class);

    /**
     * Holds a map of ConsumerKey to ConsumerEntry, which is basically a container
     * for the running consumers.
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
     * Thread pool where we run consumers within background threads.
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    /**
     * Constructor.
     * @param webKafkaConsumerFactory For creating new Consumers.
     * @param messagingTemplate For publishing consumed messages back through the web socket.
     * @param maxConcurrentConsumers Configuration, how many consumers to run.
     */
    public WebSocketConsumersManager(
        final WebKafkaConsumerFactory webKafkaConsumerFactory,
        final SimpMessagingTemplate messagingTemplate,
        final int maxConcurrentConsumers
    ) {
        this(
            webKafkaConsumerFactory,
            messagingTemplate,

            // Setup managed thread pool with number of concurrent threads.
            new ThreadPoolExecutor(
                maxConcurrentConsumers,
                maxConcurrentConsumers,
                5,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(100)
            )
        );
    }

    /**
     * Constructor for injecting a ThreadPoolExecutor.
     *
     * @param webKafkaConsumerFactory For creating new Consumers.
     * @param messagingTemplate For publishing consumed messages back through the web socket.
     * @param threadPoolExecutor executor pool for async processing.
     */
    public WebSocketConsumersManager(
        final WebKafkaConsumerFactory webKafkaConsumerFactory,
        final SimpMessagingTemplate messagingTemplate,
        final ThreadPoolExecutor threadPoolExecutor
    ) {
        this.webKafkaConsumerFactory = webKafkaConsumerFactory;
        this.simpMessagingTemplate = messagingTemplate;

        // TODO add handler for when a new connection comes in that exceeds the maximum running concurrent consumers.
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * Start up a new consumer for the given view.
     * @param view The view to consume from
     * @param startingPosition What position to resume consuming from.
     * @param sessionIdentifier The user who is consuming.
     */
    public void addNewConsumer(
        final View view,
        final Collection<FilterDefinition> filters,
        final StartingPosition startingPosition,
        final SessionIdentifier sessionIdentifier
    ) {

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
                filters,
                startingPosition,
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

    /**
     * Remove consumer based on their private session id.
     * Typically this is called when a consumer disconnects from the websocket.
     */
    public void removeConsumersForSessionId(final String sessionId) {
        synchronized (consumers) {
            consumers
                .entrySet()
                .stream()
                .filter((entry) -> entry.getKey().getSessionId().equals(sessionId))
                .forEach((entry) -> entry.getValue().requestStop());
        }
    }

    /**
     * Remove consumer based on their public session hash.
     * Typically this is called when a request to disconnect a consumer is made by the consumer manager
     * web UI.
     */
    public boolean removeConsumersForSessionHash(final String sessionHash) {
        synchronized (consumers) {
            for (final Map.Entry<ConsumerKey, ConsumerEntry> entry : consumers.entrySet()) {
                if (! entry.getKey().getSessionHash().equals(sessionHash)) {
                    continue;
                }
                entry.getValue().requestStop();
                return true;
            }
        }
        return false;
    }

    /**
     * Pause a consumer.
     */
    public void pauseConsumer(final long viewId, final SessionIdentifier sessionIdentifier) {
        // createWebClient a key
        final ConsumerKey consumerKey = new ConsumerKey(viewId, sessionIdentifier);

        synchronized (consumers) {
            if (!consumers.containsKey(consumerKey)) {
                return;
            }

            // Get entry
            final ConsumerEntry consumerEntry = consumers.get(consumerKey);

            // Lets pause it
            consumerEntry.requestPause();
        }
    }

    /**
     * Resume a consumer.
     */
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

    /**
     * @return Returns all of the currently active consumers.
     */
    public Collection<StreamConsumerDetails> getConsumers() {
        final Collection<StreamConsumerDetails> details = consumers.entrySet().stream()
            .map((entry) -> new StreamConsumerDetails(
                    entry.getKey().getUserId(),
                    entry.getKey().getViewId(),
                    entry.getKey().getSessionHash(),
                    entry.getValue().getStartTimestamp(),
                    entry.getValue().getRecordCount(),
                    entry.getValue().isPaused()
                )
            ).collect(Collectors.toList());

        // Return immutable
        return Collections.unmodifiableCollection(details);
    }

    /**
     * @return Current number of active consumers.
     */
    public int countActiveConsumers() {
        return consumers.size();
    }

    /**
     * Main processing loop for the Manager.
     */
    @Override
    public void run() {
        // Loop thru consumers, consume, and publish to socket.
        do {
            boolean foundResult = false;
            final List<ConsumerKey> consumerKeysToRemove = new ArrayList<>();

            // Loop over each consumer
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

                    // Define header so we can send the message to a specific session id.
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

                // Add logger statement, this isn't completely accurate because the thread
                // may not have shut down yet..
                logger.info("Removed web socket consumer, now has ~ {}/{} running consumers",
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
        } while (true);

        // Shut down
        threadPoolExecutor.shutdown();
    }

    /**
     * Small wrapper around the Socket Consumer.
     */
    private static final class ConsumerEntry {
        /**
         * Our wrapped SocketKafkaConsumer instance.
         */
        private final SocketKafkaConsumer socketKafkaConsumer;

        /**
         * Holds how many records we have consumed.
         */
        private final AtomicLong recordCount = new AtomicLong(0);

        /**
         * Holds when we started consuming.
         */
        private final long startTimestamp = Clock.systemUTC().millis();

        /**
         * Flag if we should requestStop.
         */
        private boolean shouldStop = false;

        /**
         * Flag if we should be paused.
         */
        private AtomicBoolean isPaused = new AtomicBoolean(false);

        /**
         * Constructor.
         * @param socketKafkaConsumer The consumer to wrap.
         */
        public ConsumerEntry(final SocketKafkaConsumer socketKafkaConsumer) {
            this.socketKafkaConsumer = socketKafkaConsumer;
        }

        /**
         * Retrieve the next record from Kafka.
         * @return Optional of KafkaResult.  This could return null if there are no new records to consume.
         */
        public Optional<KafkaResult> nextResult() {
            // If paused
            if (isPaused.get()) {
                // always return false.  This will cause the internal buffer to block
                // on the consumer side.
                return Optional.empty();
            }
            final Optional<KafkaResult> result = socketKafkaConsumer.nextResult();

            // Increment counter if record returned.
            result.ifPresent(
                (record) -> recordCount.incrementAndGet()
            );

            // Return result
            return result;
        }

        /**
         * @return True if a stop has been requested.
         */
        public synchronized boolean isShouldStop() {
            return shouldStop;
        }

        /**
         * Request the Consumer to shutdown/stop.
         */
        public synchronized void requestStop() {
            this.socketKafkaConsumer.requestStop();
            this.shouldStop = true;
        }

        /**
         * Request the consumer to be paused.
         */
        public synchronized void requestPause() {
            isPaused.set(true);
        }

        /**
         * Request the consumer to be resumed.
         */
        public synchronized void requestResume() {
            isPaused.set(false);
        }

        public long getRecordCount() {
            return recordCount.get();
        }

        public long getStartTimestamp() {
            return startTimestamp;
        }

        public boolean isPaused() {
            return isPaused.get();
        }
    }

    /**
     * Represents a unique consumer key.
     * This could probably simplified down to just the sessionId.
     */
    static final class ConsumerKey {
        // viewId, userId, and sessionId make a unique consumer key.
        private final long viewId;
        private final long userId;

        // Session id should be considered private and should not be shared with other users.
        // TODO probably should deprecate this.
        private final String sessionId;

        // Session hash can be shared publicly to identify a session.
        private final String sessionHash;

        ConsumerKey(final long viewId, final SessionIdentifier sessionIdentifier) {
            this.viewId = viewId;
            this.userId = sessionIdentifier.getUserId();
            this.sessionId = sessionIdentifier.getSessionId();
            this.sessionHash = Sha1Tools.sha1(this.sessionId);
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

        public String getSessionHash() {
            return sessionHash;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            final ConsumerKey that = (ConsumerKey) other;

            if (viewId != that.viewId) {
                return false;
            }
            if (userId != that.userId) {
                return false;
            }
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
