/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void testCalculateRanges_simple() {
        final List<String> result = Utils.calculateRanges(
            Arrays.asList(0,1,2,3,4,5,6)
        );

        assertEquals(1, result.size());
        assertEquals("0-6", result.get(0));
    }

    @Test
    public void testCalculateRanges_one() {
        final List<String> result = Utils.calculateRanges(
            Arrays.asList(0)
        );

        assertEquals(1, result.size());
        assertEquals("0", result.get(0));
    }

    @Test
    public void testCalculateRanges_empty() {
        final List<String> result = Utils.calculateRanges(
            Arrays.asList()
        );

        assertEquals(0, result.size());
    }

    @Test
    public void testCalculateRanges_multi() {
        final List<String> result = Utils.calculateRanges(
            Arrays.asList(0,1,3,4,6)
        );

        assertEquals(3, result.size());
        assertEquals("0-1", result.get(0));
        assertEquals("3-4", result.get(1));
        assertEquals("6", result.get(2));
    }

    @Test
    public void testCalculateRanges_multi2() {
        final List<String> result = Utils.calculateRanges(
            Arrays.asList(0,1,3,4,6,7)
        );

        assertEquals(3, result.size());
        assertEquals("0-1", result.get(0));
        assertEquals("3-4", result.get(1));
        assertEquals("6-7", result.get(2));
    }

    @Test
    public void testCalculateRanges_singles() {
        final List<String> result = Utils.calculateRanges(
            Arrays.asList(0,3,6)
        );

        assertEquals(3, result.size());
        assertEquals("0", result.get(0));
        assertEquals("3", result.get(1));
        assertEquals("6", result.get(2));
    }
}