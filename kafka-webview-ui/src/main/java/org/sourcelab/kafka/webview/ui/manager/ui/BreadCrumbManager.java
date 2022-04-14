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

package org.sourcelab.kafka.webview.ui.manager.ui;

import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Quick n' Dirty manager for managing breadcrumbs in the UI.
 */
public class BreadCrumbManager {
    /**
     * Contains our bread crumbs.
     */
    private List<Crumb> crumbs = new ArrayList<>();

    /**
     * Constructor.
     * @param model view Model we can add ourself onto.
     */
    public BreadCrumbManager(final Model model) {
        model.addAttribute("BreadCrumbs", this);

        // Add home by default
        addCrumb("Home", "/");
    }

    /**
     * Add new crumb to the UI linked to given URL.
     * @param name Name to display for the bread crumb.
     * @param url Url to link to
     * @return self
     */
    public BreadCrumbManager addCrumb(final String name, final String url) {
        crumbs.add(new Crumb(name, url));
        return this;
    }

    /**
     * Add new crumb to the UI without a link.
     * @param name Name to display for the bread crumb.
     * @return self
     */
    public BreadCrumbManager addCrumb(final String name) {
        crumbs.add(new Crumb(name, null));
        return this;
    }

    /**
     * Get all defined crumbs.
     */
    public List<Crumb> getCrumbs() {
        return crumbs;
    }

    /**
     * Internal class to keep track of the crumbs.
     */
    private static class Crumb {
        private final String name;
        private final String url;

        private Crumb(final String name, final String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}
