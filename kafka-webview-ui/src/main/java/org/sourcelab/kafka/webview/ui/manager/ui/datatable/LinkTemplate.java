package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Render template for rendering links.
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
