/**
 * MIT License
 *
 * Copyright (c) 2017 Stephen Powis https://github.com/Crim/kafka-webview
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

package com.darksci.kafka.webview.ui.manager.plugin;

import com.darksci.kafka.webview.ui.plugin.filter.RecordFilter;
import org.junit.Test;

import java.net.URL;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test over the PluginClassLoader.
 */
public class PluginClassLoaderTest {

    /**
     * Tests loading a class from a jar.
     */
    @Test
    public void testLoadingFilterPlugin() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // Get URL to our jar
        final URL jar = getClass().getClassLoader().getResource("testDeserializer/testPlugins.jar");
        final String classPath = "examples.filter.LowOffsetFilter";

        // Create class loader
        final PluginClassLoader pluginClassLoader = new PluginClassLoader(jar, getClass().getClassLoader());

        final Class<? extends RecordFilter> filterPlugin = (Class<? extends RecordFilter>) pluginClassLoader.loadClass(classPath);
        assertNotNull("Should not be null", filterPlugin);

        // Create an instance of it and validate.
        final RecordFilter filter = filterPlugin.newInstance();
        final String topic = "MyTopic";
        final int partition = 2;
        final long offset = 2423L;
        final Object key = "key";
        final Object value = "{name='Bob', value='value'}";
        filter.filter(topic, partition, offset, key, value);

        // Validate it came from our classloader, more of a sanity test.
        assertTrue("Should have our parent class loader", filter.getClass().getClassLoader() instanceof PluginClassLoader);

        // Validate permission set defined
        final ProtectionDomain protectionDomain = filter.getClass().getProtectionDomain();
        final PermissionCollection permissionCollection = protectionDomain.getPermissions();
        assertTrue("Should have read only permissions", permissionCollection.isReadOnly());
    }
}