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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * For rendering YES/NO Badges in a column for boolean values.
 * @param <T> The type of object being rendered on the datatable.
 */
public class YesNoBadgeTemplate<T> extends RenderTemplate<T> {

    private final Function<T, Boolean> booleanFunction;

    /**
     * Constructor.
     * @param booleanFunction Function to render yes or no.
     */
    public YesNoBadgeTemplate(final Function<T, Boolean> booleanFunction) {
        super("fragments/datatable/fields/YesNoBadge", "display");
        this.booleanFunction = Objects.requireNonNull(booleanFunction);
    }

    @Override
    List<Object> getParameters(T record) {
        final List<Object> params = new ArrayList<>();
        params.add(booleanFunction.apply(record));
        return params;
    }
}
