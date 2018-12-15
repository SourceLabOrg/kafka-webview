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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sourcelab.kafka.webview.ui.manager.encryption.Sha1Tools;
import org.sourcelab.kafka.webview.ui.manager.kafka.SessionIdentifier;
import org.sourcelab.kafka.webview.ui.manager.kafka.SocketKafkaConsumer;
import org.sourcelab.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterDefinition;
import org.sourcelab.kafka.webview.ui.model.View;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebSocketConsumersManagerTest {

    /**
     * By default expect no exceptions.
     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    // Create mocks
    private WebKafkaConsumerFactory mockConsumerFactory;
    private SimpMessagingTemplate mockMessagingTemplate;
    private ThreadPoolExecutor mockThreadPoolExecutor;
    private SocketKafkaConsumer mockSocketKafkaConsumer;

    // Create test instance passing in mocks.
    private WebSocketConsumersManager manager;

    /**
     * Setup mocks before each test.
     */
    @Before
    public void setup() {
        this.mockConsumerFactory = mock(WebKafkaConsumerFactory.class);
        this.mockMessagingTemplate = mock(SimpMessagingTemplate.class);
        this.mockThreadPoolExecutor = mock(ThreadPoolExecutor.class);
        this.mockSocketKafkaConsumer = mock(SocketKafkaConsumer.class);

        // Create instance.
        this.manager = new WebSocketConsumersManager(
            mockConsumerFactory,
            mockMessagingTemplate,
            mockThreadPoolExecutor
        );
    }

    /**
     * Verify that you can add a consumer.
     */
    @Test
    public void testAddConsumer() {
        final long userId = 1212L;
        final String sessionId = "abc123";
        final long viewId = 5454L;

        // Sanity test, manager should have no consumers.
        Assert.assertTrue("Manager should start empty", manager.getConsumers().isEmpty());

        // Define consumer inputs.
        final View view = new View();
        view.setId(viewId);

        final Collection<FilterDefinition> filters = new ArrayList<>();
        final StartingPosition startingPosition = StartingPosition.newHeadPosition();
        final SessionIdentifier sessionIdentifier = SessionIdentifier.newStreamIdentifier(userId, sessionId);

        // Configure mocks
        when(mockConsumerFactory.createWebSocketClient(view, filters, startingPosition, sessionIdentifier))
            .thenReturn(mockSocketKafkaConsumer);

        // Add consumer
        manager.addNewConsumer(view, filters, startingPosition, sessionIdentifier);

        // Verify mocks.
        verify(mockConsumerFactory, times(1))
            .createWebSocketClient(view, filters, startingPosition, sessionIdentifier);

        verify(mockThreadPoolExecutor, times(1))
            .execute(mockSocketKafkaConsumer);

        // Ask for consumers
        final Collection<StreamConsumerDetails> consumers = manager.getConsumers();
        Assert.assertFalse("Should not be empty", consumers.isEmpty());
        Assert.assertEquals(1, consumers.size());

        // Verify consumer1
        verifyConsumer(
            consumers,
            userId,
            viewId,
            false,
            0,
            sessionId
        );
    }

    /**
     * Verify you cannot add the a consumer with the same
     * ViewId + UserId + SessionId.
     */
    @Test
    public void testAddConsumer_sameConsumerTwiceFails() {
        final long userId = 1212L;
        final String sessionId = "abc123";
        final long viewId = 5454L;

        // Sanity test, manager should have no consumers.
        Assert.assertTrue("Manager should start empty", manager.getConsumers().isEmpty());

        // Define consumer inputs.
        final View view = new View();
        view.setId(viewId);

        final Collection<FilterDefinition> filters = new ArrayList<>();
        final StartingPosition startingPosition = StartingPosition.newHeadPosition();
        final SessionIdentifier sessionIdentifier = SessionIdentifier.newStreamIdentifier(userId, sessionId);

        // Configure mocks
        when(mockConsumerFactory.createWebSocketClient(view, filters, StartingPosition.newHeadPosition(), sessionIdentifier))
            .thenReturn(mockSocketKafkaConsumer);

        // Add consumer once
        manager.addNewConsumer(view, filters, startingPosition, sessionIdentifier);

        // Attempt to add again, this should throw an exception
        expectedException.expectMessage("Consumer already exists");
        expectedException.expect(RuntimeException.class);

        manager.addNewConsumer(view, filters, StartingPosition.newTailPosition(), sessionIdentifier);
    }

    /**
     * Verify you can add the a consumer with the same
     * UserId + SessionId but a different viewId.
     */
    @Test
    public void testAddConsumer_twoConsumersDifferentViewId() {
        final long userId = 1212L;
        final String sessionId = "abc123";
        final long viewId1 = 5454L;
        final long viewId2 = 5456L;

        // Sanity test, manager should have no consumers.
        Assert.assertTrue("Manager should start empty", manager.getConsumers().isEmpty());

        // Define consumer inputs.
        final View view1 = new View();
        view1.setId(viewId1);

        final View view2 = new View();
        view2.setId(viewId2);

        final Collection<FilterDefinition> filters = new ArrayList<>();
        final StartingPosition startingPosition = StartingPosition.newHeadPosition();
        final SessionIdentifier sessionIdentifier = SessionIdentifier.newStreamIdentifier(userId, sessionId);

        final SocketKafkaConsumer mockSocketKafkaConsumer1 = mock(SocketKafkaConsumer.class);
        final SocketKafkaConsumer mockSocketKafkaConsumer2 = mock(SocketKafkaConsumer.class);

        // Configure mocks
        when(mockConsumerFactory.createWebSocketClient(view1, filters, startingPosition, sessionIdentifier))
            .thenReturn(mockSocketKafkaConsumer1);

        when(mockConsumerFactory.createWebSocketClient(view2, filters, startingPosition, sessionIdentifier))
            .thenReturn(mockSocketKafkaConsumer2);

        // Add consumer once
        manager.addNewConsumer(view1, filters, startingPosition, sessionIdentifier);

        // Add another consumer, for a different view
        manager.addNewConsumer(view2, filters, startingPosition, sessionIdentifier);

        // Ask for consumers
        final Collection<StreamConsumerDetails> consumers = manager.getConsumers();
        Assert.assertFalse("Should not be empty", consumers.isEmpty());
        Assert.assertEquals(2, consumers.size());

        // Verify consumer1
        verifyConsumer(
            consumers,
            userId,
            viewId1,
            false,
            0,
            sessionId
        );

        // Verify consumer2
        verifyConsumer(
            consumers,
            userId,
            viewId2,
            false,
            0,
            sessionId
        );

        // Verify mocks for consumer 1
        verify(mockConsumerFactory, times(1))
            .createWebSocketClient(view1, filters, startingPosition, sessionIdentifier);
        verify(mockThreadPoolExecutor, times(1))
            .execute(mockSocketKafkaConsumer1);

        // Verify mocks for consumer 2
        verify(mockConsumerFactory, times(1))
            .createWebSocketClient(view2, filters, startingPosition, sessionIdentifier);
        verify(mockThreadPoolExecutor, times(1))
            .execute(mockSocketKafkaConsumer2);
    }

    /**
     * Verify you can add the a consumer with the same
     * ViewId + UserId but different sessionId.
     */
    @Test
    public void testAddConsumer_twoConsumersDifferentSessionId() {
        final long userId = 1212L;
        final String sessionId1 = "abc123";
        final String sessionId2 = "123abc";
        final long viewId = 5454L;

        // Sanity test, manager should have no consumers.
        Assert.assertTrue("Manager should start empty", manager.getConsumers().isEmpty());

        // Define consumer inputs.
        final View view = new View();
        view.setId(viewId);

        final Collection<FilterDefinition> filters = new ArrayList<>();
        final StartingPosition startingPosition = StartingPosition.newHeadPosition();
        final SessionIdentifier sessionIdentifier1 = SessionIdentifier.newStreamIdentifier(userId, sessionId1);
        final SessionIdentifier sessionIdentifier2 = SessionIdentifier.newStreamIdentifier(userId, sessionId2);

        final SocketKafkaConsumer mockSocketKafkaConsumer1 = mock(SocketKafkaConsumer.class);
        final SocketKafkaConsumer mockSocketKafkaConsumer2 = mock(SocketKafkaConsumer.class);

        // Configure mocks
        when(mockConsumerFactory.createWebSocketClient(view, filters, startingPosition, sessionIdentifier1))
            .thenReturn(mockSocketKafkaConsumer1);

        when(mockConsumerFactory.createWebSocketClient(view, filters, startingPosition, sessionIdentifier2))
            .thenReturn(mockSocketKafkaConsumer2);

        // Add consumer once
        manager.addNewConsumer(view, filters, startingPosition, sessionIdentifier1);

        // Add another consumer, with different sessionIdentifier.
        manager.addNewConsumer(view, filters, startingPosition, sessionIdentifier2);

        // Ask for consumers
        final Collection<StreamConsumerDetails> consumers = manager.getConsumers();
        Assert.assertFalse("Should not be empty", consumers.isEmpty());
        Assert.assertEquals(2, consumers.size());

        // Verify consumer1
        verifyConsumer(
            consumers,
            userId,
            viewId,
            false,
            0,
            sessionId1
        );

        // Verify consumer2
        verifyConsumer(
            consumers,
            userId,
            viewId,
            false,
            0,
            sessionId2
        );

        // Verify mocks for consumer 1
        verify(mockConsumerFactory, times(1))
            .createWebSocketClient(view, filters, startingPosition, sessionIdentifier1);
        verify(mockThreadPoolExecutor, times(1))
            .execute(mockSocketKafkaConsumer1);

        // Verify mocks for consumer 2
        verify(mockConsumerFactory, times(1))
            .createWebSocketClient(view, filters, startingPosition, sessionIdentifier2);
        verify(mockThreadPoolExecutor, times(1))
            .execute(mockSocketKafkaConsumer2);
    }

    /**
     * Verify you can add remove a consumer by sessionId.
     */
    @Test
    public void testRemoveConsumersForSessionId() {
        final long userId = 1212L;
        final String sessionId1 = "abc123";
        final String sessionId2 = "123abc";
        final long viewId = 5454L;

        // Sanity test, manager should have no consumers.
        Assert.assertTrue("Manager should start empty", manager.getConsumers().isEmpty());

        // Define consumer inputs.
        final View view = new View();
        view.setId(viewId);

        final Collection<FilterDefinition> filters = new ArrayList<>();
        final StartingPosition startingPosition = StartingPosition.newHeadPosition();
        final SessionIdentifier sessionIdentifier1 = SessionIdentifier.newStreamIdentifier(userId, sessionId1);
        final SessionIdentifier sessionIdentifier2 = SessionIdentifier.newStreamIdentifier(userId, sessionId2);

        final SocketKafkaConsumer mockSocketKafkaConsumer1 = mock(SocketKafkaConsumer.class);
        final SocketKafkaConsumer mockSocketKafkaConsumer2 = mock(SocketKafkaConsumer.class);

        // Configure mocks
        when(mockConsumerFactory.createWebSocketClient(view, filters, startingPosition, sessionIdentifier1))
            .thenReturn(mockSocketKafkaConsumer1);

        when(mockConsumerFactory.createWebSocketClient(view, filters, startingPosition, sessionIdentifier2))
            .thenReturn(mockSocketKafkaConsumer2);

        // Add consumer once
        manager.addNewConsumer(view, filters, startingPosition, sessionIdentifier1);

        // Add another consumer, with different sessionIdentifier.
        manager.addNewConsumer(view, filters, startingPosition, sessionIdentifier2);

        // Ask for consumers
        final Collection<StreamConsumerDetails> consumers = manager.getConsumers();
        Assert.assertFalse("Should not be empty", consumers.isEmpty());
        Assert.assertEquals(2, consumers.size());

        // Ask to remove by sessionId
        manager.removeConsumersForSessionId(sessionId1);

        // Verify it requested the consumer to stop.
        verify(mockSocketKafkaConsumer1, times(1)).requestStop();
        verify(mockSocketKafkaConsumer2, times(0)).requestStop();
    }

    /**
     * Verify you can add remove a consumer by sessionHash.
     */
    @Test
    public void testRemoveConsumersForSessionHash() {
        final long userId = 1212L;
        final String sessionId1 = "abc123";
        final String sessionId2 = "123abc";
        final long viewId = 5454L;

        // Sanity test, manager should have no consumers.
        Assert.assertTrue("Manager should start empty", manager.getConsumers().isEmpty());

        // Define consumer inputs.
        final View view = new View();
        view.setId(viewId);

        final Collection<FilterDefinition> filters = new ArrayList<>();
        final StartingPosition startingPosition = StartingPosition.newHeadPosition();
        final SessionIdentifier sessionIdentifier1 = SessionIdentifier.newStreamIdentifier(userId, sessionId1);
        final SessionIdentifier sessionIdentifier2 = SessionIdentifier.newStreamIdentifier(userId, sessionId2);

        final SocketKafkaConsumer mockSocketKafkaConsumer1 = mock(SocketKafkaConsumer.class);
        final SocketKafkaConsumer mockSocketKafkaConsumer2 = mock(SocketKafkaConsumer.class);

        // Configure mocks
        when(mockConsumerFactory.createWebSocketClient(view, filters, startingPosition, sessionIdentifier1))
            .thenReturn(mockSocketKafkaConsumer1);

        when(mockConsumerFactory.createWebSocketClient(view, filters, startingPosition, sessionIdentifier2))
            .thenReturn(mockSocketKafkaConsumer2);

        // Add consumer once
        manager.addNewConsumer(view, filters, startingPosition, sessionIdentifier1);

        // Add another consumer, with different sessionIdentifier.
        manager.addNewConsumer(view, filters, startingPosition, sessionIdentifier2);

        // Ask for consumers
        final Collection<StreamConsumerDetails> consumers = manager.getConsumers();
        Assert.assertFalse("Should not be empty", consumers.isEmpty());
        Assert.assertEquals(2, consumers.size());

        // Ask to remove by sessionId
        boolean result = manager.removeConsumersForSessionHash(Sha1Tools.sha1(sessionId1));
        Assert.assertTrue(result);

        // Verify it requested the consumer to stop.
        verify(mockSocketKafkaConsumer1, times(1)).requestStop();
        verify(mockSocketKafkaConsumer2, times(0)).requestStop();

        // Now ask for a random hash
        result = manager.removeConsumersForSessionHash("made up");
        Assert.assertFalse(result);
    }

    /**
     * Verify you can pause a consumer.
     */
    @Test
    public void testRequestPause() {
        final long userId = 1212L;
        final String sessionId1 = "abc123";
        final String sessionId2 = "123abc";
        final long viewId = 5454L;

        // Sanity test, manager should have no consumers.
        Assert.assertTrue("Manager should start empty", manager.getConsumers().isEmpty());

        // Define consumer inputs.
        final View view = new View();
        view.setId(viewId);

        final Collection<FilterDefinition> filters = new ArrayList<>();
        final StartingPosition startingPosition = StartingPosition.newHeadPosition();
        final SessionIdentifier sessionIdentifier1 = SessionIdentifier.newStreamIdentifier(userId, sessionId1);
        final SessionIdentifier sessionIdentifier2 = SessionIdentifier.newStreamIdentifier(userId, sessionId2);

        final SocketKafkaConsumer mockSocketKafkaConsumer1 = mock(SocketKafkaConsumer.class);
        final SocketKafkaConsumer mockSocketKafkaConsumer2 = mock(SocketKafkaConsumer.class);

        // Configure mocks
        when(mockConsumerFactory.createWebSocketClient(view, filters, startingPosition, sessionIdentifier1))
            .thenReturn(mockSocketKafkaConsumer1);

        when(mockConsumerFactory.createWebSocketClient(view, filters, startingPosition, sessionIdentifier2))
            .thenReturn(mockSocketKafkaConsumer2);

        // Add consumer once
        manager.addNewConsumer(view, filters, startingPosition, sessionIdentifier1);

        // Add another consumer, with different sessionIdentifier.
        manager.addNewConsumer(view, filters, startingPosition, sessionIdentifier2);

        // Ask to pause consumer 1
        manager.pauseConsumer(viewId, sessionIdentifier1);

        // Ask for consumers
        Collection<StreamConsumerDetails> consumers = manager.getConsumers();
        Assert.assertFalse("Should not be empty", consumers.isEmpty());
        Assert.assertEquals(2, consumers.size());

        // Verify consumer1 is paused.
        verifyConsumer(
            consumers,
            userId,
            viewId,
            true,
            0,
            sessionId1
        );

        // Verify consumer2 is not paused.
        verifyConsumer(
            consumers,
            userId,
            viewId,
            false,
            0,
            sessionId2
        );

        // Attempt to resume
        manager.resumeConsumer(viewId, sessionIdentifier1);

        // Ask for consumers
        consumers = manager.getConsumers();
        Assert.assertFalse("Should not be empty", consumers.isEmpty());
        Assert.assertEquals(2, consumers.size());

        // Verify consumer1 is not paused.
        verifyConsumer(
            consumers,
            userId,
            viewId,
            false,
            0,
            sessionId1
        );

        // Verify consumer2 is not paused.
        verifyConsumer(
            consumers,
            userId,
            viewId,
            false,
            0,
            sessionId2
        );
    }

    /**
     * Helper method to validate ConsumerDetails.
     */
    private void verifyConsumer(
        final Collection<StreamConsumerDetails> consumerDetails,
        final long userId,
        final long viewId,
        final boolean isPaused,
        final long recordCount,
        final String sessionId
    ) {

        final String targetSessionHash = Sha1Tools.sha1(sessionId);

        int foundCount = 0;

        for (final StreamConsumerDetails consumerDetail : consumerDetails) {
            if (!targetSessionHash.equals(consumerDetail.getSessionHash())) {
                continue;
            }

            if (userId != consumerDetail.getUserId()) {
                continue;
            }

            if (viewId != consumerDetail.getViewId()) {
                continue;
            }

            if (isPaused != consumerDetail.isPaused()) {
                continue;
            }

            if (recordCount != consumerDetail.getRecordCount()) {
                continue;
            }
            foundCount++;
        }

        Assert.assertEquals("incorrect match found!", 1, foundCount);
    }
}