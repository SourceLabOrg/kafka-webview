/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import java.time.Instant;
import java.util.Date;

/**
 * StreamConsumerDetails.
 * Immutable value class holding information about a consumer.
 */
public final class StreamConsumerDetails {
    private final long viewId;
    private final long userId;
    private final String sessionHash;
    private final long startedAtTimestamp;
    private final long recordCount;
    private final boolean isPaused;

    /**
     * Constructor.
     * @param viewId id of the view the consumer is consuming.
     * @param userId id of the userx consuming.
     * @param sessionHash public session hash.
     * @param startedAtTimestamp unix timestamp of when consumer was created.
     * @param recordCount How many records the consumer has consumed.
     * @param isPaused if the consumer is currently paused.
     */
    public StreamConsumerDetails(
        final long userId,
        final long viewId,
        final String sessionHash,
        final long startedAtTimestamp,
        final long recordCount,
        final boolean isPaused) {
        this.viewId = viewId;
        this.userId = userId;
        this.sessionHash = sessionHash;
        this.startedAtTimestamp = startedAtTimestamp;
        this.recordCount = recordCount;
        this.isPaused = isPaused;
    }

    public long getUserId() {
        return userId;
    }

    public long getViewId() {
        return viewId;
    }

    public String getSessionHash() {
        return sessionHash;
    }

    public long getStartedAtTimestamp() {
        return startedAtTimestamp;
    }

    public Date getStartedAtDate() {
        return Date.from( Instant.ofEpochSecond( getStartedAtTimestamp() ) );
    }

    public long getRecordCount() {
        return recordCount;
    }

    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public String toString() {
        return "StreamConsumerDetails{"
            + "userId=" + getUserId()
            + ", viewId=" + getViewId()
            + ", startedAtTimestamp=" + startedAtTimestamp
            + ", recordCount=" + recordCount
            + ", isPaused=" + isPaused
            + '}';
    }
}
