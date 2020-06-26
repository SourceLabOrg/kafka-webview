package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

/**
 * Defines an action link at the top of a datatable.
 */
public class DatatableLink {
    private final String url;
    private final String label;
    private final String icon;

    /**
     * Constructor.
     * @param url Url to link to.
     * @param label Label for the link.
     */
    public DatatableLink(final String url, final String label, final String icon) {
        this.url = url;
        this.label = label;
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public boolean hasIcon() {
        if (icon == null || icon.isEmpty()) {
            return false;
        }
        return true;
    }
}
