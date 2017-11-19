package org.sourcelab.kafka.webview.ui.manager.socket;

import java.util.Map;

/**
 * Represents where the streaming client should start consuming from.
 */
public class StartingPosition {
    private final Position position;
    private final Map<Integer, Long> offsetsOrTimestampsMap;

    private StartingPosition(final Position position) {
        this.position = position;
        offsetsOrTimestampsMap = null;
    }

    private StartingPosition(final Position position, final Map<Integer, Long> offsetsOrTimestampsMap) {
        this.position = position;
        this.offsetsOrTimestampsMap = offsetsOrTimestampsMap;
    }

    public boolean isStartFromHead() {
        return Position.HEAD == position;
    }

    public boolean isStartFromTail() {
        return Position.TAIL == position;
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
     * @param timestamps
     * @return
     */
    public static StartingPosition newPositionFromTimestamps(final Map<Integer, Long> timestamps) {
        return new StartingPosition(Position.TIMESTAMP, timestamps);
    }

    /**
     * @param timestamps
     * @return
     */
    public static StartingPosition newPositionFromOffsets(final Map<Integer, Long> timestamps) {
        return new StartingPosition(Position.OFFSETS, timestamps);
    }
}
