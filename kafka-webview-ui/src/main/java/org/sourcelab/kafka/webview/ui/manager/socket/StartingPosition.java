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

package org.sourcelab.kafka.webview.ui.manager.socket;

import java.util.Map;

/**
 * Represents where the streaming client should start consuming from.
 */
public class StartingPosition {
    private final Position position;
    private final Map<Integer, Long> offsetsMap;
    private final long timestamp;

    /**
     * Constructor for Tail/Head positions.
     * @param position Define what position to resume from.
     */
    private StartingPosition(final Position position) {
        this.position = position;
        this.offsetsMap = null;
        this.timestamp = 0L;
    }

    private StartingPosition(final Map<Integer, Long> offsetsMap) {
        this.position = Position.OFFSETS;
        this.offsetsMap = offsetsMap;
        this.timestamp = 0L;
    }

    private StartingPosition(final long timestamp) {
        this.position = Position.TIMESTAMP;
        this.offsetsMap = null;
        this.timestamp = timestamp;
    }

    /**
     * @return True if we're configured to resume from HEAD.
     */
    public boolean isStartFromHead() {
        return Position.HEAD == position;
    }

    /**
     * @return True if we're configured to resume from TAIL.
     */
    public boolean isStartFromTail() {
        return Position.TAIL == position;
    }

    /**
     * @return True if we're configured to resume from specified timestamp.
     */
    public boolean isStartFromTimestamp() {
        return Position.TIMESTAMP == position;
    }

    /**
     * @return True if we're configured to resume from specified offsets.
     */
    public boolean isStartFromOffsets() {
        return Position.OFFSETS == position;
    }

    /**
     * @return configured timestamp to resume consuming from.
     */
    public long getTimestamp() {
        if (!isStartFromTimestamp()) {
            throw new IllegalStateException("Cannot access timestamp when position is type " + position);
        }
        return timestamp;
    }

    /**
     * @return configured offsets to resume consuming from.
     */
    public Map<Integer, Long> getOffsetsMap() {
        if (!isStartFromOffsets()) {
            throw new IllegalStateException("Cannot access offsets when position is type " + position);
        }
        return offsetsMap;
    }

    /**
     * Enumerate the various starting states.
     */
    private enum Position {
        // Resume from existing state (falls back to head)
        EXISTING_STATE,

        // Start from Head of topic.
        HEAD,

        // Start from Tail of topic.
        TAIL,

        // Start from specified offsets.
        OFFSETS,

        // Start from specified timestamp.
        TIMESTAMP;
    }

    /**
     * @return New StartingPosition instance configured to start from HEAD.
     */
    public static StartingPosition newHeadPosition() {
        return new StartingPosition(Position.HEAD);
    }

    /**
     *
     * @return New StartingPosition instance configured to start from TAIL.
     */
    public static StartingPosition newTailPosition() {
        return new StartingPosition(Position.TAIL);
    }

    /**
     * @param timestamp Unix timestamp (in milliseconds) of where to resume consuming from.
     * @return New StartingPosition instance configured to start from the supplied timestamp.
     */
    public static StartingPosition newPositionFromTimestamp(final long timestamp) {
        return new StartingPosition(timestamp);
    }

    /**
     * @param offsets Maps from Partition => Offset.  Any not supplied offsets will resume from HEAD.
     * @return New StartingPosition instance configured to start from the supplied offsets map.
     */
    public static StartingPosition newPositionFromOffsets(final Map<Integer, Long> offsets) {
        return new StartingPosition(offsets);
    }

    /**
     * @return New StartingPosition instance configured to start from existing consumer state. Falls back to HEAD.
     */
    public static StartingPosition newResumeFromExistingState() {
        return new StartingPosition(Position.EXISTING_STATE);
    }
}
