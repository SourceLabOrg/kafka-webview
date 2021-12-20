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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Render template for rendering links.
 * @param <T> The type of object being rendered on the datatable.
 */
public class LinkTemplate<T> extends RenderTemplate<T> {

    private final Function<T, String> urlFunction;
    private final Function<T, String> textFunction;

    /**
     * Constructor.
     * @param urlFunction Function to render the URL for the link.
     * @param textFunction Function to render the text for the link.
     */
    public LinkTemplate(Function<T, String> urlFunction, Function<T, String> textFunction) {
        super("fragments/datatable/fields/Link", "display");
        this.urlFunction = urlFunction;
        this.textFunction = textFunction;
    }

    @Override
    List<Object> getParameters(T record) {
        final List<Object> params = new ArrayList<>();
        params.add(urlFunction.apply(record));
        params.add(textFunction.apply(record));
        return params;
    }
}
