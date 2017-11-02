package com.darksci.kafka.webview.ui.manager.kafka;

public class SessionIdentifier {
    private final long userId;
    private final String sessionId;

    public SessionIdentifier(final long userId, final String sessionId) {
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SessionIdentifier that = (SessionIdentifier) o;

        if (userId != that.userId) return false;
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
