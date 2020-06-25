package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

/**
 * Defines an action link at the top of a datatable.
 */
public class DatatableLink {
    private final String url;
    private final String label;

    /**
     * Constructor.
     * @param url Url to link to.
     * @param label Label for the link.
     */
    public DatatableLink(final String url, final String label) {
        this.url = url;
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public String getLabel() {
        return label;
    }
}
