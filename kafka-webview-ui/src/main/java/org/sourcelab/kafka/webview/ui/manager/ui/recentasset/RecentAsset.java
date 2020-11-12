package org.sourcelab.kafka.webview.ui.manager.ui.recentasset;


public class RecentAsset {
    private final String name;
    private final long id;
    private final String url;

    public RecentAsset(final String name, final long id, final String url) {
        this.name = name;
        this.id = id;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "RecentAsset{"
            + "name='" + name + '\''
            + ", id=" + id
            + ", url='" + url + '\''
            + '}';
    }
}
