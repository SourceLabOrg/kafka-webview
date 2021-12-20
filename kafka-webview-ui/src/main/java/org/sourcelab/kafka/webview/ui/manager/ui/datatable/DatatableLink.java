/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

    /**
     * Does this link have an icon?
     * @return true if yes, false if not.
     */
    public boolean hasIcon() {
        if (icon == null || icon.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DatatableLink{"
            + "url='" + url + '\''
            + ", label='" + label + '\''
            + ", icon='" + icon + '\''
            + '}';
    }
}
