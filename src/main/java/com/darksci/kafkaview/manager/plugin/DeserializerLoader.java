package com.darksci.kafkaview.manager.plugin;

import com.darksci.kafkaview.manager.plugin.exception.LoaderException;
import com.darksci.kafkaview.manager.plugin.exception.UnableToFindClassException;
import com.darksci.kafkaview.manager.plugin.exception.WrongImplementationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class DeserializerLoader {
    private final String jarDirectory;

    public DeserializerLoader(final String deserializerJarPath) {
        jarDirectory = deserializerJarPath;
    }


    public Class<? extends Deserializer> getDeserializerClass(final String jarName, final String classpath) throws LoaderException {
        try {
            final String absolutePath = getPathForJar(jarName).toString();
            final URL jarUrl = new URL("file://" + absolutePath);
            final ClassLoader pluginClassLoader = new PluginClassLoader(jarUrl);
            final Class loadedClass = pluginClassLoader.loadClass(classpath);

            if (!Deserializer.class.isAssignableFrom(loadedClass)) {
                throw new WrongImplementationException("Class does not implement " + Deserializer.class.getName(), null);
            }

            return loadedClass;
        } catch (MalformedURLException exception) {
            throw new LoaderException("Unable to load jar " + jarName, exception);
        } catch (ClassNotFoundException exception) {
            throw new UnableToFindClassException("Unable to find class " + classpath + " in jar " + jarName, exception);
        }
    }

    public Deserializer getDeserializer(final String jarName, final String classpath) throws LoaderException {
        Class<? extends Deserializer> dClass = getDeserializerClass(jarName, classpath);
        try {
            return dClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new LoaderException(e.getMessage(), e);
        }
    }

    public String getJarDirectory() {
        return jarDirectory;
    }

    public Path getPathForJar(final String jarName) {
        return new File(jarDirectory + "/" + jarName).toPath().toAbsolutePath();
    }
}
