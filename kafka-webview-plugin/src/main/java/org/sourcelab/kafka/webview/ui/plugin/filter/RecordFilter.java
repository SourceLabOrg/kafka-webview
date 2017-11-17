/**
 * MIT License
 *
 * Copyright (c) 2017 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.plugin.filter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Interface that defines a Record Filter.
 */
public interface RecordFilter {
    /**
     * Define names of configurable options.
     * These names will be passed up to the User Interface and allow the user to define them.
     * When configure() is called below, these same names will be returned, along with the user defined values,
     * in the filterOptions argument.
     *
     * Since the UI provides no validation on these user defined values, best practices dictate that your implementation
     * should gracefully handle when these are not set.
     *
     * @return Set of option names.
     */
    default Set<String> getOptionNames() {
        return new HashSet<>();
    }

    /**
     * Configure this class.
     * @param consumerConfigs Consumer configuration in key/value pairs
     * @param filterOptions User defined filter options.
     */
    void configure(final Map<String, ?> consumerConfigs, final Map<String, String> filterOptions);

    /**
     * Define the filter behavior.
     * A return value of TRUE means the record WILL be shown.
     * A return value of FALSE means the record will NOT be shown.
     *
     * @param topic Name of topic the message came from.
     * @param partition Partition the message came from.
     * @param offset Offset the message came from.
     * @param key Deserialized Key object.
     * @param value Deserialized Value object.
     * @return True means the record WILL be shown.  False means the record will NOT be shown.
     */
    boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value);

    /**
     * Called on closing.
     */
    void close();
}
