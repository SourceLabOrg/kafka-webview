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

import org.junit.Assert;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.kafka.SessionIdentifier;

public class ConsumerKeyTest {
    @Test
    public void testEquals_differentInstances() {
        final long viewId = 123L;
        final long userId = 444L;
        final String sessionId = "BlahBlah";

        final WebSocketConsumersManager.ConsumerKey consumerKey1 = new WebSocketConsumersManager.ConsumerKey(viewId, SessionIdentifier.newStreamIdentifier(userId, sessionId));
        final WebSocketConsumersManager.ConsumerKey consumerKey2 = new WebSocketConsumersManager.ConsumerKey(viewId, SessionIdentifier.newStreamIdentifier(userId, sessionId));

        Assert.assertTrue(consumerKey1.equals(consumerKey2));
    }

    @Test
    public void testEquals_sameInstance() {
        final long viewId = 123L;
        final long userId = 444L;
        final String sessionId = "BlahBlah";

        final WebSocketConsumersManager.ConsumerKey consumerKey1 = new WebSocketConsumersManager.ConsumerKey(viewId, SessionIdentifier.newStreamIdentifier(userId, sessionId));

        Assert.assertTrue(consumerKey1.equals(consumerKey1));
    }

    @Test
    public void testEquals_differentViewIds() {
        final long viewId1 = 123L;
        final long viewId2 = 321L;
        final long userId = 444L;
        final String sessionId = "BlahBlah";

        final WebSocketConsumersManager.ConsumerKey consumerKey1 = new WebSocketConsumersManager.ConsumerKey(viewId1, SessionIdentifier.newStreamIdentifier(userId, sessionId));
        final WebSocketConsumersManager.ConsumerKey consumerKey2 = new WebSocketConsumersManager.ConsumerKey(viewId2, SessionIdentifier.newStreamIdentifier(userId, sessionId));

        Assert.assertFalse(consumerKey1.equals(consumerKey2));
    }
}
