/**
 * MIT License
 *
 * Copyright (c) 2017 SourceLab.org (https://github.com/Crim/kafka-webview/)
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
