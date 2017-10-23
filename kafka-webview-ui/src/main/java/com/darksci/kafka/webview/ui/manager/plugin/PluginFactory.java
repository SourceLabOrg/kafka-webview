package com.darksci.kafka.webview.ui.manager.plugin;

import com.darksci.kafka.webview.ui.manager.plugin.exception.LoaderException;
import com.darksci.kafka.webview.ui.manager.plugin.exception.UnableToFindClassException;
import com.darksci.kafka.webview.ui.manager.plugin.exception.WrongImplementationException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

/**
 * A factory class for creating instances of uploaded plugins.
 * Currently this supports two types of plugins: Deserializers, and RecordFilters.
 *
 * @param <T> Interface/Type of plugin we want to create instances of.
 */
public class PluginFactory<T> {
    /**
     * Directory where JARs can be loaded from.
     */
    private final String jarDirectory;

    /**
     * Type/Interface of class we want to create instances of.
     */
    private final Class<T> typeParameterClass;

    /**
     * Constructor.
     * @param jarDirectory Where we can load JARs from.
     * @param typeParameterClass The type/interface of classes we can create instances of.
     */
    public PluginFactory(final String jarDirectory, final Class<T> typeParameterClass) {
        this.jarDirectory = jarDirectory;
        this.typeParameterClass = typeParameterClass;
    }

    /**
     * Get the Class instance of the plugin at the given classpath loaded using the
     * standard JVM ClassLoader.  Used to load default deserializers.
     *
     * @param classpath Classpath to class to load.
     * @return Class instance of the given classpath.
     * @throws LoaderException When we run into issues loading the class.
     *
     * TODO we may want to not use this and only load via our class loader?
     */
    public Class<? extends T> getPluginClass(final String classpath) throws LoaderException {
        return getPluginClass(getClass().getClassLoader(), classpath);
    }

    /**
     * Get the Class instance of the plugin at the given classpath loaded from within the provided
     * JAR.
     *
     * @param jarName Filename of the JAR to load the class from.
     * @param classpath Classpath to class to load.
     * @return Class instance of the given classpath.
     * @throws LoaderException When we run into issues loading the class.
     */
    public Class<? extends T> getPluginClass(final String jarName, final String classpath) throws LoaderException {
        try {
            final String absolutePath = getPathForJar(jarName).toString();
            final URL jarUrl = new URL("file://" + absolutePath);
            final ClassLoader pluginClassLoader = new PluginClassLoader(jarUrl, getClass().getClassLoader());
            //final ClassLoader pluginClassLoader = new PluginClassLoader(jarUrl);
            return getPluginClass(pluginClassLoader, classpath);
        } catch (MalformedURLException exception) {
            throw new LoaderException("Unable to load jar " + jarName, exception);
        }
    }

    /**
     * Internal method to load the given classpath using the given ClassLoader.
     * @return Class instance.
     * @throws LoaderException When we run into issues loading the class.
     */
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

    /**
     * Create an instance of the class given at the classpath loaded from the given Jar.
     * @param jarName Jar to load the class from
     * @param classpath Classpath to class.
     * @return Instance of the class.
     * @throws LoaderException When we run into issues.
     */
    public T getPlugin(final String jarName, final String classpath) throws LoaderException {
        final Class<? extends T> dClass = getPluginClass(jarName, classpath);
        try {
            return dClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new LoaderException(e.getMessage(), e);
        }
    }

    /**
     * Create an instance of the class given at the classpath loaded from the JVM's ClassLoader.
     * @param classpath Classpath to class.
     * @return Instance of the class.
     * @throws LoaderException When we run into issues.
     *
     * TODO - Remove this method?
     */
    private T getPlugin(final String classpath) throws LoaderException {
        Class<? extends T> dClass = getPluginClass(classpath);
        try {
            return dClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new LoaderException(e.getMessage(), e);
        }
    }

    private String getJarDirectory() {
        return jarDirectory;
    }

    private void checkSecurityManager() {
        // Sanity test of environment?
        if (System.getSecurityManager() == null) {
            throw new RuntimeException("You should have a security manager loaded!");
        }
    }

    /**
     * Get the full path on disk to the given Jar file.
     * @param jarName Jar to lookup full path to.
     */
    public Path getPathForJar(final String jarName) {
        return new File(jarDirectory + "/" + jarName).toPath().toAbsolutePath();
    }
}
