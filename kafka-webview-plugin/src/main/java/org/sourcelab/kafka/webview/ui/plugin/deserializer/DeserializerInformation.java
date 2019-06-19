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

package org.sourcelab.kafka.webview.ui.plugin.deserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * POJO deserializer information.
 */
public class DeserializerInformation {

    private final String name;
    private final String classpath;
    private final Map<String, String> defaultConfig;

    public DeserializerInformation(String name, Class<?> clazz) {
        this(name, clazz.getName());
    }

    public DeserializerInformation(String name, String classpath) {
        this(name, classpath,  new HashMap<>());
    }

    public DeserializerInformation(String name, Class<?> clazz, Map<String, String> defaultConfig) {
        this(name, clazz.getName(), defaultConfig);
    }

    /**
     * Constructor.
     * 
     * @param name Deserializer name.
     * @param classpath Deserializer class full name, must implement org.apache.kafka.common.serialization.Deserializer.
     * @param defaultConfig Default configuration of the deserializer.
     */
    public DeserializerInformation(String name, String classpath, Map<String, String> defaultConfig) {
        this.name = name;
        this.classpath = classpath;
        this.defaultConfig = defaultConfig;
    }

    public String getName() {
        return name;
    }

    public String getClasspath() {
        return classpath;
    }

    public Map<String, String> getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public String toString() {
        return "DeserializerInformation{"
                + "name=" + name
                + ", classpath=" + classpath
                + ", defaultConfig=" + defaultConfig
                + '}';
    }
}
