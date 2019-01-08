/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import org.junit.Test;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.mockito.ArgumentMatchers.eq;
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