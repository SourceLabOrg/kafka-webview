package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

import java.util.List;

/**
 *
 */
public class Page<T> {
    private final List<T> data;
    private final int recordsFiltered;
    private final int recordsTotal;
    private final int draw;

    public Page(final List<T> data, final int recordsFiltered, final int recordsTotal, final int draw) {
        this.data = data;
        this.recordsFiltered = recordsFiltered;
        this.recordsTotal = recordsTotal;
        this.draw = draw;
    }

    public List<T> getData() {
        return data;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public int getDraw() {
        return draw;
    }

    @Override
    public String toString() {
        return "Page{"
            + "data=" + data
            + ", recordsFiltered=" + recordsFiltered
            + ", recordsTotal=" + recordsTotal
            + ", draw=" + draw
            + '}';
    }
}
