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

package org.sourcelab.kafka.webview.ui.manager.kafka;

import org.sourcelab.kafka.webview.ui.manager.encryption.Sha1Tools;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Unique key to represent a session.
 * Combines userId + sessionId.
 */
public class SessionIdentifier {
    private final long userId;
    private final String sessionId;
    private final Context context;

    /**
     * Constructor.
     */
    public SessionIdentifier(
        final long userId,
        @NotNull final String sessionId,
        @NotNull final Context context
    ) {
        if (sessionId == null) {
            throw new NullPointerException("sessionId cannot be null!");
        }
        this.userId = userId;

        // Use hash of sessionId to avoid exposure
        this.sessionId = sessionId;
        this.context = context;
    }

    public long getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final SessionIdentifier that = (SessionIdentifier) other;

        if (userId != that.userId) {
            return false;
        }

        if (context != that.context) {
            return false;
        }
        return sessionId.equals(that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, sessionId, context);
    }

    @Override
    public String toString() {
        return userId + "-" + context.name + "-" + Sha1Tools.sha1(sessionId);
    }

    /**
     * Factory method.
     */
    public static SessionIdentifier newWebIdentifier(final long userId, final String sessionId) {
        return new SessionIdentifier(userId, sessionId, Context.WEB);
    }

    /**
     * Factory method.
     */
    public static SessionIdentifier newStreamIdentifier(final long userId, final String sessionId) {
        return new SessionIdentifier(userId, sessionId, Context.STREAM);
    }

    /**
     * Enumerate Context types.
     */
    public static enum Context {
        WEB("web"),
        STREAM("stream");

        String name;

        Context(final String name) {
            this.name = name;
        }
    }
}
