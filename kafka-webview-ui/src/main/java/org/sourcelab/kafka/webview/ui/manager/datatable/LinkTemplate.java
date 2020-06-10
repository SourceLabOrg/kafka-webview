package org.sourcelab.kafka.webview.ui.manager.datatable;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 */
public class LinkTemplate<T> extends RenderTemplate<T> {

    private final Function<T, String> urlFunction;
    private final Function<T, String> textFunction;

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
