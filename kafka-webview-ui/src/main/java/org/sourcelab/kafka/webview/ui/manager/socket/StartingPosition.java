package org.sourcelab.kafka.webview.ui.manager.socket;

import java.util.Map;

/**
 * Represents where the streaming client should start consuming from.
 */
public class StartingPosition {
    private final Position position;
    private final Map<Integer, Long> offsetsMap;
    private final long timestamp;
    private boolean startFromOffsets;

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

    public boolean isStartFromHead() {
        return Position.HEAD == position;
    }

    public boolean isStartFromTail() {
        return Position.TAIL == position;
    }

    public boolean isStartFromTimestamp() {
        return Position.TIMESTAMP == position;
    }

    public boolean isStartFromOffsets() {
        return Position.OFFSETS == position;
    }

    public long getTimestamp() {
        if (!isStartFromTimestamp()) {
            throw new IllegalStateException("Cannot access timestamp when position is type " + position);
        }
        return timestamp;
    }

    public Map<Integer, Long> getOffsetsMap() {
        if (!isStartFromOffsets()) {
            throw new IllegalStateException("Cannot access offsets when position is type " + position);
        }
        return offsetsMap;
    }

    private enum Position {
        HEAD,
        TAIL,
        OFFSETS,
        TIMESTAMP;
    }

    public static StartingPosition newHeadPosition() {
        return new StartingPosition(Position.HEAD);
    }

    /**
     *
     * @return
     */
    public static StartingPosition newTailPosition() {
        return new StartingPosition(Position.TAIL);
    }

    /**
     *
     * @param timestamp
     * @return
     */
    public static StartingPosition newPositionFromTimestamp(final long timestamp) {
        return new StartingPosition(timestamp);
    }

    /**
     * @param offsets
     * @return
     */
    public static StartingPosition newPositionFromOffsets(final Map<Integer, Long> offsets) {
        return new StartingPosition(offsets);
    }
}
