package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

/**
 *
 */
public class Column {
    private final String data;
    private final String name;
    private final Boolean searchable;
    private final Boolean orderable;
    private final Search search;

    public Column(final String data, final String name, final Boolean searchable, final Boolean orderable, final Search search) {
        this.data = data;
        this.name = name;
        this.searchable = searchable;
        this.orderable = orderable;
        this.search = search;
    }

    public String getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public Boolean getSearchable() {
        return searchable;
    }

    public Boolean getOrderable() {
        return orderable;
    }

    public Search getSearch() {
        return search;
    }

    @Override
    public String toString() {
        return "Column{"
            + "data='" + data + '\''
            + ", name='" + name + '\''
            + ", searchable=" + searchable
            + ", orderable=" + orderable
            + ", search=" + search
            + '}';
    }
}
