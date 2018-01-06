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

package org.sourcelab.kafka.webview.ui.manager.plugin;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.LoaderException;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PluginFactoryTest {

    /**
     * Test creating a RecordFilter.
     */
    @Test
    public void testWithRecordFilter() throws LoaderException {
        final String jarFilename = "testPlugins.jar";
        final String classPath = "examples.filter.LowOffsetFilter";

        // Find jar on filesystem.
        final URL jar = getClass().getClassLoader().getResource("testDeserializer/" + jarFilename);
        final String jarPath = new File(jar.getFile()).getParent();

        // Create factory
        final PluginFactory<RecordFilter> factory = new PluginFactory<>(jarPath, RecordFilter.class);
        final Path pathForJar = factory.getPathForJar(jarFilename);

        // Validate path is correct
        assertEquals("Has expected Path", jar.getPath(), pathForJar.toString());

        // Get class instance
        final Class<? extends RecordFilter> pluginFilterClass = factory.getPluginClass(jarFilename, classPath);

        // Validate
        assertNotNull(pluginFilterClass);
        assertEquals("Has expected name", classPath, pluginFilterClass.getName());
        assertTrue("Validate came from correct class loader", pluginFilterClass.getClassLoader() instanceof PluginClassLoader);

        // Crete filter instance
        final RecordFilter recordFilter = factory.getPlugin(jarFilename, classPath);
        assertNotNull(recordFilter);
        assertEquals("Has correct name", classPath, recordFilter.getClass().getName());

        // Call method on interface
        recordFilter.displayRecord("topic", 1, 1L, "Key", "Value");
    }

    /**
     * Test creating a Deserializer.
     */
    @Test
    public void testWithDeserializer() throws LoaderException {
        final String jarFilename = "testPlugins.jar";
        final String classPath = "examples.deserializer.ExampleDeserializer";

        // Find jar on filesystem.
        final URL jar = getClass().getClassLoader().getResource("testDeserializer/" + jarFilename);
        final String jarPath = new File(jar.getFile()).getParent();

        // Create factory
        final PluginFactory<Deserializer> factory = new PluginFactory<>(jarPath, Deserializer.class);
        final Path pathForJar = factory.getPathForJar(jarFilename);

        // Validate path is correct
        assertEquals("Has expected Path", jar.getPath(), pathForJar.toString());

        // Get class instance
        final Class<? extends Deserializer> pluginFilterClass = factory.getPluginClass(jarFilename, classPath);

        // Validate
        assertNotNull(pluginFilterClass);
        assertEquals("Has expected name", classPath, pluginFilterClass.getName());
        assertTrue("Validate came from correct class loader", pluginFilterClass.getClassLoader() instanceof PluginClassLoader);

        // Crete filter instance
        final Deserializer deserializer = factory.getPlugin(jarFilename, classPath);
        assertNotNull(deserializer);
        assertEquals("Has correct name", classPath, deserializer.getClass().getName());

        // Call method on interface
        final String value = "MyValue";
        final String result = (String) deserializer.deserialize("MyTopic", value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tests loading a deserializer not from an external jar.
     */
    @Test
    public void testLoadingDefaultDeserializer() throws LoaderException {
        final String classPath = StringDeserializer.class.getName();

        // Create factory
        final PluginFactory<Deserializer> factory = new PluginFactory<>("/tmp", Deserializer.class);

        // Get class instance
        final Class<? extends Deserializer> pluginFilterClass = factory.getPluginClass(classPath);

        // Validate
        assertNotNull(pluginFilterClass);
        assertEquals("Has expected name", classPath, pluginFilterClass.getName());
    }
}