package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

/**
 * Represents a sorting order for a specific column.
 */
public class Order {
    private final Integer column;
    private final Direction dir;

    public Order(final Integer column, final Direction dir) {
        this.column = column;
        this.dir = dir;
    }

    public Integer getColumn() {
        return column;
    }

    public Direction getDir() {
        return dir;
    }

    @Override
    public String toString() {
        return "Order{"
            + "column=" + column
            + ", dir=" + dir
            + '}';
    }
}
