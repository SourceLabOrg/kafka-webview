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

package org.sourcelab.kafka.webview.ui.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Collection of one-off utility methods.
 */
public class Utils {

    /**
     * Given a Collection of numbers like [1,2,3,4,6,8,9,10,11,13] this will return Collection like:
     * [1-4,6,8-11,13]
     *
     * Far from the most ideal algorithm for this.
     *
     * @param numbers Numbers to generate ranges for,
     * @return Calculated ranges.
     */
    public static List<String> calculateRanges(final Collection<Integer> numbers) {
        // Copy list
        final List<Integer> sorted = new ArrayList<>();
        sorted.addAll(numbers);

        // Ensure numbers are sorted
        Collections.sort(sorted, Comparator.naturalOrder());

        // TODO filter duplicate numbers out?

        final List<String> ranges = new ArrayList<>();

        if (sorted.size() == 1) {
            ranges.add(String.valueOf(sorted.get(0)));
            return ranges;
        }

        final Iterator<Integer> numberIterator = numbers.iterator();
        if (!numberIterator.hasNext()) {
            return new ArrayList<>();
        }

        // Start calculating range?
        Integer first = null;
        Integer last = null;
        do {
            Integer number = numberIterator.next();

            if (first == null) {
                first = number;
                last = number;

                if (!numberIterator.hasNext()) {
                    ranges.add(String.valueOf(first));
                }
                continue;
            }

            if (number.equals(last + 1)) {
                last = number;

                if (!numberIterator.hasNext()) {
                    ranges.add(String.valueOf(first) + "-" + String.valueOf(last));
                }
                continue;
            } else {
                if (first.equals(last)) {
                    ranges.add(String.valueOf(first));
                } else {
                    ranges.add(String.valueOf(first) + "-" + String.valueOf(last));
                }
                first = number;
                last = number;

                if (!numberIterator.hasNext()) {
                    ranges.add(String.valueOf(first));
                }
                continue;
            }


        } while (numberIterator.hasNext());


        return ranges;
    }
}
