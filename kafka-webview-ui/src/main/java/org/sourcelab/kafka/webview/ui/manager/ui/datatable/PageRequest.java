package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

import java.util.List;

/**
 * Represents a request for a page of records for use on a Datatable UI.
 */
public class PageRequest {
    private final int start;
    private final int length;
    private final int draw;
    private final List<Order> order;
    private final List<Column> columns;
    private final Search search;

    public PageRequest(final int start, final int length, final int draw, final List<Order> order, final List<Column> columns, final Search search) {
        this.start = start;
        this.length = length;
        this.draw = draw;
        this.order = order;
        this.columns = columns;
        this.search = search;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    public int getDraw() {
        return draw;
    }

    public List<Order> getOrder() {
        return order;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public Search getSearch() {
        return search;
    }

    @Override
    public String toString() {
        return "PageRequest{"
            + "start=" + start
            + ", length=" + length
            + ", draw=" + draw
            + ", order=" + order
            + ", columns=" + columns
            + ", search=" + search
            + '}';
    }
}
