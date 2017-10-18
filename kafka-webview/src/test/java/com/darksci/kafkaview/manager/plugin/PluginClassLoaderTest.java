package com.darksci.kafkaview.manager.plugin;

import com.darksci.kafkaview.plugin.filter.RecordFilter;
import org.junit.Test;

import java.net.URL;
import java.security.Policy;

import static org.junit.Assert.assertNotNull;

/**
 * Test over the PluginClassLoader.
 */
public class PluginClassLoaderTest {

    /**
     * Tests loading a class from a jar w/o a security policy enforced.
     */
    @Test
    public void testLoadingClassWithoutSecurityPolicy() throws ClassNotFoundException {
        // Get URL to our jar
        final URL jar = getClass().getClassLoader().getResource("testDeserializer/testPlugins.jar");
        final String classPath = "com.example.myplugins.LowOffsetFilter";

        // Create class loader
        final PluginClassLoader pluginClassLoader = new PluginClassLoader(jar, getClass().getClassLoader());

        final Class<? extends RecordFilter> filterPlugin = (Class<? extends RecordFilter>) pluginClassLoader.loadClass(classPath);
        assertNotNull("Should not be null", filterPlugin);
    }

    /**
     * Tests loading a class from a jar with a security policy enforced.
     */
    @Test
    public void testLoadingClassWithSecurityPolicy() throws ClassNotFoundException {
        final Policy pluginSecurityPolicy = new PluginSecurityPolicy();
        Policy.setPolicy(pluginSecurityPolicy);
        System.setSecurityManager(new SecurityManager());

        // Get URL to our jar
        final URL jar = getClass().getClassLoader().getResource("testDeserializer/testPlugins.jar");
        final String classPath = "com.example.myplugins.LowOffsetFilter";

        // Create class loader
        final PluginClassLoader pluginClassLoader = new PluginClassLoader(jar);

        final Class<? extends RecordFilter> filterPlugin = (Class<? extends RecordFilter>) pluginClassLoader.loadClass(classPath, true);
        assertNotNull("Should not be null", filterPlugin);
    }

}