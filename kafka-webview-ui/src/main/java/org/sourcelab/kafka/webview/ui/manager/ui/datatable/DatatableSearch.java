/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

/**
 * Represents a searchable field on a datatable.
 */
public class DatatableSearch {
    private final String label;
    private final List<String> fields;
    private final String currentSearchTerm;

    /**
     * Constructor.
     * @param label Display label.
     * @param fields One or more fields to search across.
     * @param currentSearchTerm The current search term if defined in a request.
     */
    public DatatableSearch(final String label, final List<String> fields, final String currentSearchTerm) {
        Objects.requireNonNull(fields);

        this.label = Objects.requireNonNull(label);
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
        this.currentSearchTerm = currentSearchTerm;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getFields() {
        return fields;
    }

    public String getCurrentSearchTerm() {
        return currentSearchTerm;
    }

    @Override
    public String toString() {
        return "DatatableSearch{"
            + "label='" + label + '\''
            + ", fields=" + fields
            + ", currentSearchTerm='" + currentSearchTerm + '\''
            + '}';
    }
}
