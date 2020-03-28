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

package org.sourcelab.kafka.webview.ui.manager.plugin;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Before;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.file.FileManager;
import org.sourcelab.kafka.webview.ui.manager.file.FileStorageService;
import org.sourcelab.kafka.webview.ui.manager.file.FileType;
import org.sourcelab.kafka.webview.ui.manager.file.LocalDiskStorage;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.LoaderException;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PluginFactoryTest {

    private PluginFactory<Deserializer> deserializerPluginFactory;
    private PluginFactory<RecordFilter> recordFilterPluginFactory;

    private FileManager fileManager;

    @Before
    public void setup() {
        // Determine root path to test resources testFiles/ directory.
        final URL testFilesDirectory = getClass().getClassLoader().getResource("testFiles");
        final String uploadPath = testFilesDirectory.getPath();

        final LocalDiskStorage fileStorageService = new LocalDiskStorage(uploadPath);

        // Create file Manager
        fileManager = new FileManager(fileStorageService, fileStorageService);

        deserializerPluginFactory = new PluginFactory<>(FileType.DESERIALIZER, Deserializer.class, fileManager);
        recordFilterPluginFactory = new PluginFactory<>(FileType.FILTER, RecordFilter.class, fileManager);
    }

    /**
     * Test creating a RecordFilter.
     */
    @Test
    public void testWithRecordFilter() throws LoaderException, IOException {
        final String jarFilename = "testPlugins.jar";
        final String classPath = "examples.filter.LowOffsetFilter";

        // Find jar on filesystem.
        final URL jar = getClass().getClassLoader().getResource("testFiles/filters/" + jarFilename);

        // Create factory
        final PluginFactory<RecordFilter> factory = recordFilterPluginFactory;
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
        recordFilter.includeRecord("topic", 1, 1L, "Key", "Value");
    }

    /**
     * Test checking a RecordFilter.
     */
    @Test
    public void testCheckPlugin_WithRecordFilter() throws LoaderException, IOException {
        final String jarFilename = "testPlugins.jar";
        final String classPath = "examples.filter.LowOffsetFilter";

        // Find jar on filesystem.
        final URL jar = getClass().getClassLoader().getResource("testFiles/filters/" + jarFilename);

        // Create factory
        final PluginFactory<RecordFilter> factory = recordFilterPluginFactory;
        final Path pathForJar = factory.getPathForJar(jarFilename);

        // Validate path is correct
        assertEquals("Has expected Path", jar.getPath(), pathForJar.toString());

        // Get class instance
        final Class<? extends RecordFilter> pluginFilterClass = factory.getPluginClass(jarFilename, classPath);

        // Validate
        assertNotNull(pluginFilterClass);
        assertEquals("Has expected name", classPath, pluginFilterClass.getName());
        assertTrue("Validate came from correct class loader", pluginFilterClass.getClassLoader() instanceof PluginClassLoader);

        // Check filter instance
        final boolean result = factory.checkPlugin(jarFilename, classPath);
        assertTrue(result);
    }

    /**
     * Test creating a Deserializer.
     */
    @Test
    public void testWithDeserializer() throws LoaderException, IOException {
        final String jarFilename = "testPlugins.jar";
        final String classPath = "examples.deserializer.ExampleDeserializer";

        // Find jar on filesystem.
        final URL jar = getClass().getClassLoader().getResource("testFiles/deserializers/" + jarFilename);

        // Create factory
        final PluginFactory<Deserializer> factory = deserializerPluginFactory;
        final Path pathForJar = factory.getPathForJar(jarFilename);

        // Validate path is correct
        assertEquals("Has expected Path", jar.getPath(), pathForJar.toString());

        // Get class instance
        final Class<? extends Deserializer> pluginFilterClass = factory.getPluginClass(jarFilename, classPath);

        // Validate
        assertNotNull(pluginFilterClass);
        assertEquals("Has expected name", classPath, pluginFilterClass.getName());
        assertTrue("Validate came from correct class loader", pluginFilterClass.getClassLoader() instanceof PluginClassLoader);

        // Crete Deserializer instance
        final Deserializer deserializer = factory.getPlugin(jarFilename, classPath);
        assertNotNull(deserializer);
        assertEquals("Has correct name", classPath, deserializer.getClass().getName());

        // Call method on interface
        final String value = "MyValue";
        final String result = (String) deserializer.deserialize("MyTopic", value.getBytes(StandardCharsets.UTF_8));
        assertEquals("Prefixed Value: " + value, result);
    }

    /**
     * Test checking a Deserializer.
     */
    @Test
    public void testCheckPlugin_WithDeserializer() throws LoaderException, IOException {
        final String jarFilename = "testPlugins.jar";
        final String classPath = "examples.deserializer.ExampleDeserializer";

        // Find jar on filesystem.
        final URL jar = getClass().getClassLoader().getResource("testFiles/deserializers/" + jarFilename);
        final String jarPath = new File(jar.getFile()).getParent();

        // Create factory
        final PluginFactory<Deserializer> factory = deserializerPluginFactory;
        final Path pathForJar = factory.getPathForJar(jarFilename);

        // Validate path is correct
        assertEquals("Has expected Path", jar.getPath(), pathForJar.toString());

        // Get class instance
        final Class<? extends Deserializer> pluginFilterClass = factory.getPluginClass(jarFilename, classPath);

        // Validate
        assertNotNull(pluginFilterClass);
        assertEquals("Has expected name", classPath, pluginFilterClass.getName());
        assertTrue("Validate came from correct class loader", pluginFilterClass.getClassLoader() instanceof PluginClassLoader);

        // Check Deserializer instance
        final boolean result = factory.checkPlugin(jarFilename, classPath);
        assertTrue(result);
    }

    /**
     * Tests loading a deserializer not from an external jar.
     */
    @Test
    public void testLoadingDefaultDeserializer() throws LoaderException {
        final String classPath = StringDeserializer.class.getName();

        // Create factory
        final PluginFactory<Deserializer> factory = new PluginFactory<>(FileType.DESERIALIZER, Deserializer.class, fileManager);

        // Get class instance
        final Class<? extends Deserializer> pluginFilterClass = factory.getPluginClass(classPath);

        // Validate
        assertNotNull(pluginFilterClass);
        assertEquals("Has expected name", classPath, pluginFilterClass.getName());
    }
}