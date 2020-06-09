package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

/**
 *
 */
public class Search {
    private final String value;
    private final String regexp;

    public Search(final String value, final String regexp) {
        this.value = value;
        this.regexp = regexp;
    }

    public String getValue() {
        return value;
    }

    public String getRegexp() {
        return regexp;
    }

    @Override
    public String toString() {
        return "Search{"
            + "value='" + value + '\''
            + ", regexp='" + regexp + '\''
            + '}';
    }
}
