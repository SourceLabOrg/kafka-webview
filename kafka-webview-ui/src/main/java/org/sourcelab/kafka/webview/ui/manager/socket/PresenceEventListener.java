package org.sourcelab.kafka.webview.ui.manager.socket;

import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Listens for when clients disconnect, and shuts down any consumers they have running.
 */
public class PresenceEventListener {
    /**
     * This manages any running consumer instances.
     */
    private final WebSocketConsumersManager webSocketConsumersManager;

    /**
     * Constructor.
     * @param webSocketConsumersManager Manages running consumer instances.
     */
    public PresenceEventListener(final WebSocketConsumersManager webSocketConsumersManager) {
        this.webSocketConsumersManager = webSocketConsumersManager;
    }

    /**
     * Called when a web socket disconnects.  We'll close out any consumers that web socket client had running
     * based on their sessionId.
     */
    @EventListener
    void handleSessionDisconnect(final SessionDisconnectEvent event) {
        // Grab sessionId from event
        final String sessionId = event.getSessionId();

        // Disconnect that sessionId's consumers
        webSocketConsumersManager.removeConsumersForSessionId(sessionId);
    }
}
