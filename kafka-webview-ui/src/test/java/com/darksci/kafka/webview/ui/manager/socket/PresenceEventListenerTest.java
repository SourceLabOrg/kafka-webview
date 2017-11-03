package com.darksci.kafka.webview.ui.manager.socket;

import org.junit.Test;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PresenceEventListenerTest {

    /**
     * Tests that we request to disconnect based on the session id.
     */
    @Test
    public void testDisconnectListner() {
        final String sessionId = "my-session-id";
        final SessionDisconnectEvent mockEvent = mock(SessionDisconnectEvent.class);
        when(mockEvent.getSessionId()).thenReturn(sessionId);

        // Create mock Manager
        final WebSocketConsumersManager mockManager = mock(WebSocketConsumersManager.class);

        // Create our listener
        final PresenceEventListener eventListener = new PresenceEventListener(mockManager);

        // Call method
        eventListener.handleSessionDisconnect(mockEvent);

        // validate
        verify(mockManager, times(1)).removeConsumersForSessionId(eq(sessionId));
    }
}