package com.darksci.kafka.webview.ui.manager.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Listens for when clients disconnect.
 */
public class PresenceEventListener {
    private final static Logger logger = LoggerFactory.getLogger(PresenceEventListener.class);


    private final WebSocketConsumersManager webSocketConsumersManager;

    public PresenceEventListener(final WebSocketConsumersManager webSocketConsumersManager) {
        this.webSocketConsumersManager = webSocketConsumersManager;
    }

    @EventListener
    private void handleSessionDisconnect(final SessionDisconnectEvent event) {
        // Decode headers
        final SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        logger.info("Disconnect event: {}", headers.getUser().getName());

        // Determine which user this is
        final long userId = getLoggedInUserId(headers);

        // Disconnect their consumers
        webSocketConsumersManager.removeConsumersForUser(userId);
    }

    private long getLoggedInUserId(final SimpMessageHeaderAccessor headers) {
        // TODO
        final Principal principal = headers.getUser();
        return 1L;
    }
}
