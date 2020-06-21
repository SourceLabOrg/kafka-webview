package org.sourcelab.kafka.webview.ui.manager.ui.datatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * For rendering YES/NO Badges in a column for boolean values.
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
