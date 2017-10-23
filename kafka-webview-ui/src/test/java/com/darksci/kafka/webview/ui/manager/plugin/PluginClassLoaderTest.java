package com.darksci.kafka.webview.ui.manager.plugin;

import com.darksci.kafka.webview.ui.plugin.filter.RecordFilter;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Test over the PluginClassLoader.
 */
public class PluginClassLoaderTest {

    /**
     * Tests loading a class from a jar.
     */
    @Test
    public void badLoad() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // Get URL to our jar
        final URL jar = getClass().getClassLoader().getResource("testDeserializer/testPlugins.jar");
        final String classPath = "com.darksci.kafka.webview.plugin.examples.filter.LowOffsetFilter";

        // Create class loader
        final PluginClassLoader pluginClassLoader = new PluginClassLoader(jar);

        final Class<? extends RecordFilter> filterPlugin = (Class<? extends RecordFilter>) pluginClassLoader.loadClass(classPath);
        assertNotNull("Should not be null", filterPlugin);

        final RecordFilter filter = filterPlugin.newInstance();
        final String topic = "MyTopic";
        final int partition = 2;
        final long offset = 2423L;
        final Object key = new String("key");
        final Object value = "{name='poop', value='value'}";
        filter.filter(topic, partition, offset, key, value);
    }
}