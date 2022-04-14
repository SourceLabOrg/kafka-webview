/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Provides Action links template.
 * @param <T> Record type.
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

    /**
     * Create new ActionLink Builder instance.
     * @param type Record type.
     * @param <T> Record type.
     * @return new Builder instance.
     */
    public static <T> Builder<T> newBuilder(Class<T> type) {
        return new Builder<>();
    }

    /**
     * Defines an Action Link.
     * @param <T> Record type.
     */
    public static class ActionLink<T> {
        private final Function<T, String> urlFunction;
        private final Function<T, String> labelFunction;
        private final String icon;
        private final boolean isPost;

        /**
         * Constructor.  See Builder instance.
         * @param urlFunction Function to generate URL.
         * @param labelFunction Function to generate label.
         * @param icon Icon to display.
         * @param isPost Should the link be a POST or get.
         */
        public ActionLink(
            final Function<T, String> urlFunction,
            final Function<T, String> labelFunction,
            final String icon,
            final boolean isPost
        ) {
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

        /**
         * Does this action link have an icon.
         * @return True if yes, false if not.
         */
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

    /**
     * ActionLink Builder instance.
     * @param <T> Record type.
     */
    public static class Builder<T> {
        private List<ActionLink<T>> links = new ArrayList<>();

        private Builder() {
        }

        /**
         * Add multiple links.
         * @param links Links to add.
         * @return Builder instance.
         */
        public Builder<T> withLinks(List<ActionLink<T>> links) {
            Objects.requireNonNull(links);
            this.links.clear();
            this.links.addAll(links);
            return this;
        }

        public Builder<T> withLink(final ActionLink<T> link) {
            this.links.add(link);
            return this;
        }

        /**
         * Add a standard Edit link.
         * @param type Record type.
         * @param urlFunction The url to link to.
         * @return Builder instance.
         */
        public Builder<T> withEditLink(
            final Class<T> type,
            final Function<T, String> urlFunction
        ) {
            return withLink(
                ActionTemplate.ActionLink.newBuilder(type)
                    .withLabelFunction((record) -> "Edit")
                    .withUrlFunction(urlFunction)
                    .withIcon("fa-edit")
                    .build()
            );
        }

        /**
         * Add a standard Delete link.
         * @param type Record type.
         * @param urlFunction The url to link to.
         * @return Builder instance.
         */
        public Builder<T> withDeleteLink(
            final Class<T> type,
            final Function<T, String> urlFunction
        ) {
            return withLink(
                ActionTemplate.ActionLink.newBuilder(type)
                    .withLabelFunction((record) -> "Delete")
                    .withUrlFunction(urlFunction)
                    .withIcon("fa-remove")
                    .withIsPost(true)
                    .build()
            );
        }

        /**
         * Add a standard Copy link.
         * @param type Record type.
         * @param urlFunction The url to link to.
         * @return Builder instance.
         */
        public Builder<T> withCopyLink(
            final Class<T> type,
            final Function<T, String> urlFunction
        ) {
            return withLink(
                ActionTemplate.ActionLink.newBuilder(type)
                    .withLabelFunction((record) -> "Copy")
                    .withUrlFunction(urlFunction)
                    .withIcon("fa-copy")
                    .withIsPost(true)
                    .build()
            );
        }

        /**
         * Create new ActionTemplate instance from Builder.
         * @return ActionType instance.
         */
        public ActionTemplate<T> build() {
            return new ActionTemplate<T>(links);
        }
    }

    /**
     * Link builder instance.
     * @param <T> Record type.
     */
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
