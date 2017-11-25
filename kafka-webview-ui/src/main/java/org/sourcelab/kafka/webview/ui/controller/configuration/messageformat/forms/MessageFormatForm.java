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

package org.sourcelab.kafka.webview.ui.controller.configuration.messageformat.forms;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents the form to create/update a MessageFormat.
 */
public class MessageFormatForm {
    private Long id = null;

    @NotNull(message = "Enter a unique name")
    @Size(min = 2, max = 255)
    private String name;

    @NotNull(message = "Enter a classpath")
    @Size(min = 2, max = 1024)
    private String classpath;

    private MultipartFile file;

    /**
     * Names of custom options.
     */
    private List<String> customOptionNames = new ArrayList<>();

    /**
     * Values of custom options.
     */
    private List<String> customOptionValues = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(final String classpath) {
        this.classpath = classpath;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(final MultipartFile file) {
        this.file = file;
    }

    public List<String> getCustomOptionNames() {
        return customOptionNames;
    }

    public void setCustomOptionNames(final List<String> customOptionNames) {
        this.customOptionNames = customOptionNames;
    }

    public List<String> getCustomOptionValues() {
        return customOptionValues;
    }

    public void setCustomOptionValues(final List<String> customOptionValues) {
        this.customOptionValues = customOptionValues;
    }

    /**
     * Utility method to return custom options as a map.
     */
    public Map<String, String> getCustomOptionsAsMap() {
        // Build a map of Name => Value
        final Map<String, String> mappedOptions = new HashMap<>();

        final Iterator<String> names = getCustomOptionNames().iterator();
        final Iterator<String> values = getCustomOptionValues().iterator();

        while (names.hasNext()) {
            final String name = names.next();
            final String value;
            if (values.hasNext()) {
                value = values.next();
            } else {
                value = "";
            }
            mappedOptions.put(name, value);
        }
        return mappedOptions;
    }

    /**
     * Does the MessageFormat that this form represents already exist in the database.
     */
    public boolean exists() {
        return getId() != null;
    }

    @Override
    public String toString() {
        return "MessageFormatForm{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", classpath='" + classpath + '\''
            + ", file=" + file
            + ", customOptionNames=" + customOptionNames
            + ", customOptionValues=" + customOptionValues
            + '}';
    }
}
