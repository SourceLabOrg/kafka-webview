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

package org.sourcelab.kafka.webview.ui.tools;

import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;
import org.sourcelab.kafka.webview.ui.repository.FilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Helpful tools for Filters in tests.
 */
@Component
public class FilterTestTools {
    private final FilterRepository filterRepository;
    private final PluginFactory<RecordFilter> recordFilterPluginFactory;

    @Autowired
    public FilterTestTools(final FilterRepository filterRepository, final PluginFactory<RecordFilter> recordFilterPluginFactory) {
        this.filterRepository = filterRepository;
        this.recordFilterPluginFactory = recordFilterPluginFactory;
    }

    /**
     * Utility for creating Filters.
     * @param name Name of the filter.
     * @return Persisted Filter.
     */
    public Filter createFilter(final String name) {
        return createFilter(name, name, name + ".jar", "{\"key\": \"value\"}");
    }

    /**
     * Utility for creating Filters.
     * @param name Name of the filter.
     * @param classPath Classpath of filter.
     * @param jarName name of the resulting jar.
     * @param options Json formatted string of options.
     * @return Persisted Filter.
     */
    public Filter createFilter(final String name, final String classPath, final String jarName, final String options) {
        final Filter filter = new Filter();
        filter.setName(name);
        filter.setClasspath(classPath);
        filter.setJar(jarName);
        filter.setOptions(options);
        filterRepository.save(filter);

        return filter;
    }

    /**
     * Using the testPlugins.jar from test resources, create a filter.
     * @param name Name of the filter.
     * @param classPath Classpath to filter within the testPlugins.jar
     * @return Persisted Filter.
     * @throws IOException on file copy issues.
     */
    public Filter createFilterFromTestPlugins(final String name, final String classPath) throws IOException {
        final String jarFileName = name + ".jar";
        final Path outputPath = recordFilterPluginFactory.getPathForJar(jarFileName);

        // Copy plugin file over
        try (final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream("testDeserializer/testPlugins.jar")) {
            Files.createDirectories(outputPath.getParent().toAbsolutePath());
            Files.copy(fileInputStream, outputPath);
        }

        return createFilter(name, classPath, jarFileName, "{}");
    }
}
