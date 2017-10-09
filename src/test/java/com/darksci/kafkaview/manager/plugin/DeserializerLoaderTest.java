package com.darksci.kafkaview.manager.plugin;

import com.darksci.kafkaview.manager.plugin.exception.LoaderException;
import org.apache.kafka.common.serialization.Deserializer;
import org.junit.Test;

import java.net.MalformedURLException;

public class DeserializerLoaderTest {

    @Test
    public void testLoad() throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException, LoaderException {
        final String jarPath = "/Users/stephen.powis/Documents/code/testdeserializers/target";
        final String jarName = "test-deserializers-1.0-SNAPSHOT.jar";
        final String classpath = "com.darksci.plugins.TestDeserializer";

        final DeserializerLoader loader = new DeserializerLoader(jarPath);
        final Deserializer deserializer = loader.getDeserializer(jarName, classpath);
        deserializer.close();
    }
}