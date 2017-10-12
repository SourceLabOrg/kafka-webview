package com.darksci.kafkaview.manager.plugin;

import com.darksci.kafkaview.manager.plugin.exception.LoaderException;
import com.darksci.kafkaview.manager.plugin.exception.UnableToFindClassException;
import com.darksci.kafkaview.manager.plugin.exception.WrongImplementationException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class PluginFactory<T> {
    private final String jarDirectory;
    private final Class<T> typeParameterClass;

    public PluginFactory(final String jarDirectory, final Class<T> typeParameterClass) {
        this.jarDirectory = jarDirectory;
        this.typeParameterClass = typeParameterClass;
    }

    public Class<? extends T> getPluginClass(final String classpath) throws LoaderException {
        return getPluginClass(getClass().getClassLoader(), classpath);
    }

    public Class<? extends T> getPluginClass(final String jarName, final String classpath) throws LoaderException {
        try {
            final String absolutePath = getPathForJar(jarName).toString();
            final URL jarUrl = new URL("file://" + absolutePath);
            final ClassLoader pluginClassLoader = new PluginClassLoader(jarUrl);
            return getPluginClass(pluginClassLoader, classpath);
        } catch (MalformedURLException exception) {
            throw new LoaderException("Unable to load jar " + jarName, exception);
        }
    }

    private Class<? extends T> getPluginClass(final ClassLoader classLoader, final String classpath) throws LoaderException {
        final Class loadedClass;
        try {
            loadedClass = classLoader.loadClass(classpath);
            if (!typeParameterClass.isAssignableFrom(loadedClass)) {
                throw new WrongImplementationException("Class does not implement " + typeParameterClass.getName(), null);
            }
            return loadedClass;
        } catch (ClassNotFoundException exception) {
            throw new UnableToFindClassException("Unable to find class " + classpath, exception);
        }
    }

    public T getPlugin(final String jarName, final String classpath) throws LoaderException {
        Class<? extends T> dClass = getPluginClass(jarName, classpath);
        try {
            return dClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new LoaderException(e.getMessage(), e);
        }
    }

    public T getPlugin(final String classpath) throws LoaderException {
        Class<? extends T> dClass = getPluginClass(classpath);
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
