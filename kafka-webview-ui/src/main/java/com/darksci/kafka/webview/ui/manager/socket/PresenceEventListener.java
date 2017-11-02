package com.darksci.kafka.webview.ui.manager.socket;

import com.darksci.kafka.webview.ui.manager.user.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Listens for when clients disconnect, and shuts down any consumers they have running.
 */
public class PresenceEventListener {
    private final static Logger logger = LoggerFactory.getLogger(PresenceEventListener.class);

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
     * Called when a websocket disconnects.  We'll close out any consumers that websocket client had running.
     */
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
        return getLoggedInUser(headers).getUserId();
    }

    private CustomUserDetails getLoggedInUser(final SimpMessageHeaderAccessor headers) {
        return (CustomUserDetails)((UsernamePasswordAuthenticationToken)headers.getUser()).getPrincipal();
    }
}
