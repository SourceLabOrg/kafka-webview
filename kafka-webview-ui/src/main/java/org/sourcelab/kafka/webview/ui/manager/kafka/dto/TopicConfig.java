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

package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Represents a collection of metadata about how a topic is configured.
 */
public class TopicConfig {
    /**
     * It may make more sense to make this into a map for faster direct access by name in the future.
     */
    private final List<ConfigItem> configEntries;

    /**
     * Constructor.
     */
    public TopicConfig(final List<ConfigItem> configEntries) {
        // Create new list from source.
        final List<ConfigItem> sorted = new ArrayList<>(configEntries);

        // Sort the list by name
        sorted.sort(Comparator.comparing(ConfigItem::getName));

        // Make immutable.
        this.configEntries = Collections.unmodifiableList(sorted);
    }

    public List<ConfigItem> getConfigEntries() {
        return configEntries;
    }

    /**
     * Given a configuration name, return its entry.
     * @param name name of config value to find.
     * @return Optionally, the matching ConfigItem.
     */
    public Optional<ConfigItem> getConfigItemByName(final String name) {
        return getConfigEntries()
            .stream()
            .filter((entry) -> name.equals(entry.getName()))
            .findFirst();
    }

    @Override
    public String toString() {
        return "TopicConfig{"
            + "configEntries=" + configEntries
            + '}';
    }
}
