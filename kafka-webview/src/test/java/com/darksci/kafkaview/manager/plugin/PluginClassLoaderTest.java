package com.darksci.kafkaview.manager.plugin;

import com.darksci.kafkaview.plugin.filter.RecordFilter;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Policy;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

/**
 * Test over the PluginClassLoader.
 */
public class PluginClassLoaderTest {

//    /**
//     * Tests loading a class from a jar w/o a security policy enforced.
//     */
//    @Test
//    public void testLoadingClassWithoutSecurityPolicy() throws ClassNotFoundException {
//        // Get URL to our jar
//        final URL jar = getClass().getClassLoader().getResource("testDeserializer/testPlugins.jar");
//        final String classPath = "com.example.myplugins.LowOffsetFilter";
//
//        // Create class loader
//        final PluginClassLoader pluginClassLoader = new PluginClassLoader(jar, getClass().getClassLoader());
//
//        final Class<? extends RecordFilter> filterPlugin = (Class<? extends RecordFilter>) pluginClassLoader.loadClass(classPath);
//        assertNotNull("Should not be null", filterPlugin);
//    }

    /**
     * Tests loading a class from a jar with a security policy enforced.
     */
    @Test
    public void testLoadingClassWithSecurityPolicy() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final Policy pluginSecurityPolicy = new PluginSecurityPolicy();
        Policy.setPolicy(pluginSecurityPolicy);
        System.setSecurityManager(new SecurityManager());

        // Get URL to our jar
        final URL jar = getClass().getClassLoader().getResource("testDeserializer/testPlugins.jar");
        final String classPath = "com.example.myplugins.LowOffsetFilter";

        // Create class loader
        final PluginClassLoader pluginClassLoader = new PluginClassLoader(jar);

        final Class<? extends RecordFilter> filterPlugin = (Class<? extends RecordFilter>) pluginClassLoader.loadClass(classPath);
        assertNotNull("Should not be null", filterPlugin);

        filterPlugin.newInstance().configure(new HashMap<>());

    }

    /**
     * Tests loading a class from a jar with a security policy enforced.
     */
    @Test
    public void badLoad() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final Policy pluginSecurityPolicy = new PluginSecurityPolicy();
        Policy.setPolicy(pluginSecurityPolicy);
        System.setSecurityManager(new SecurityManager());

        // Get URL to our jar
        final URL jar = getClass().getClassLoader().getResource("testDeserializer/badPlugin.jar");
        final String classPath = "com.example.myplugins.BadFilter";

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

    /**
     * Tests loading a class from a jar with a security policy enforced.
     */
    @Test
    public void badLoadWithNoSM() throws IllegalAccessException, InstantiationException, ClassNotFoundException, IOException {

        // Get URL to our jar
        final URL jar = getClass().getClassLoader().getResource("testDeserializer/badPlugin.jar");
        final String classPath = "com.example.myplugins.BadFilter";

        // Create class loader
        //final NewClassLoader pluginClassLoader = new NewClassLoader(new File(jar.getFile()));
        final NewClassLoader pluginClassLoader = NewClassLoader.create(new File(jar.getFile()));

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