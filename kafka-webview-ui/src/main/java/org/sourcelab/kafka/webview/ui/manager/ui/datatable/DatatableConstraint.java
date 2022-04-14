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

import java.util.Objects;

/**
 * Enforced constraint on datatable query.
 */
public class DatatableConstraint {
    private final String field;
    private final Object value;
    private final ConstraintOperator operator;

    /**
     * Constructor.
     * @param field The field to constrain.
     * @param value The value to constraint to.
     * @param operator Which operator to use.
     */
    public DatatableConstraint(final String field, final Object value, final ConstraintOperator operator) {
        this.field = Objects.requireNonNull(field);
        this.value = Objects.requireNonNull(value);
        this.operator = Objects.requireNonNull(operator);
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public ConstraintOperator getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return "DatatableConstraint{"
            + "field='" + field + '\''
            + ", value=" + value
            + ", operator=" + operator
            + '}';
    }
}
