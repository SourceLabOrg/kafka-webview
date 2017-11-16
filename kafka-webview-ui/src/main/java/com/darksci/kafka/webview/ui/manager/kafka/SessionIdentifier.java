package com.darksci.kafka.webview.ui.manager.kafka;

import javax.validation.constraints.NotNull;

/**
 * Unique key to represent a session.
 * Combines userId + sessionId.
 */
public class SessionIdentifier {
    private final long userId;
    private final String sessionId;

    /**
     * Constructor.
     */
    public SessionIdentifier(final long userId, @NotNull final String sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("sessionId cannot be null!");
        }
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public long getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
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
        return sessionId.equals(that.sessionId);
    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + sessionId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return userId + "-" + sessionId;
    }
}
