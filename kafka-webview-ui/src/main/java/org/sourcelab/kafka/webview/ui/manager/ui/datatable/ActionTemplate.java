package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Provides Action links template.
 */
public class ActionTemplate<T> extends RenderTemplate<T> {
    private final List<ActionLink<T>> links;

    /**
     * Constructor.
     */
    public ActionTemplate(final List<ActionLink<T>> actions) {
        super("fragments/datatable/fields/Action", "display");
        this.links = Collections.unmodifiableList(new ArrayList<>(actions));
    }

    public List<ActionLink<T>> getLinks() {
        return links;
    }

    @Override
    List<Object> getParameters(final T record) {
        // Only parameters are our links.
        final List<Object> params = new ArrayList<>();
        params.add("_record_");
        params.add("_this_");
        return params;
    }

    public static <T> Builder<T> newBuilder(Class<T> type) {
        return new Builder<>();
    }

    public static class ActionLink<T> {
        private final Function<T, String> urlFunction;
        private final Function<T, String> labelFunction;
        private final String icon;
        private final boolean isPost;

        public ActionLink(final Function<T, String> urlFunction, final Function<T, String> labelFunction, final String icon, final boolean isPost) {
            this.urlFunction = urlFunction;
            this.labelFunction = labelFunction;
            this.icon = icon;
            this.isPost = isPost;
        }

        public static <T> ActionLinkBuilder<T> newBuilder(final Class<T> type) {
            return new ActionLinkBuilder<T>();
        }

        public String getUrl(T record) {
            return urlFunction.apply(record);
        }

        public String getLabel(T record) {
            return labelFunction.apply(record);
        }

        public String getIcon() {
            return icon;
        }

        public boolean hasIcon() {
            if (icon == null || icon.trim().isEmpty()) {
                return false;
            }
            return true;
        }

        public boolean isPost() {
            return isPost;
        }
    }

    public static class Builder<T> {
        private List<ActionLink<T>> links = new ArrayList<>();

        private Builder() {
        }

        public Builder<T> withLinks(List<ActionLink<T>> links) {
            this.links.clear();
            this.links.addAll(links);
            return this;
        }

        public Builder<T> withLink(final ActionLink<T> link) {
            this.links.add(link);
            return this;
        }

        public ActionTemplate<T> build() {
            return new ActionTemplate<T>(links);
        }
    }

    public static class ActionLinkBuilder<T> {
        private Function<T, String> urlFunction;
        private Function<T, String> labelFunction;
        private boolean isPost = false;
        private String icon;

        public ActionLinkBuilder<T> withUrlFunction(final Function<T, String> urlFunction) {
            this.urlFunction = urlFunction;
            return this;
        }

        public ActionLinkBuilder<T> withLabelFunction(final Function<T, String> textFunction) {
            this.labelFunction = textFunction;
            return this;
        }

        public ActionLinkBuilder<T> withIsPost(final boolean post) {
            isPost = post;
            return this;
        }

        public ActionLinkBuilder<T> withIcon(final String icon) {
            this.icon = icon;
            return this;
        }

        public ActionLink<T> build() {
            return new ActionLink<T>(urlFunction, labelFunction, icon, isPost);
        }
    }
}
