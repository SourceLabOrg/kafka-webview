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

import org.sourcelab.kafka.webview.ui.manager.file.FileManager;
import org.sourcelab.kafka.webview.ui.manager.file.FileStorageService;
import org.sourcelab.kafka.webview.ui.manager.file.FileType;
import org.sourcelab.kafka.webview.ui.manager.file.LocalDiskStorage;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.LoaderException;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.UnableToFindClassException;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.WrongImplementationException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * A factory class for creating instances of uploaded plugins.
 * Currently this supports two types of plugins: Deserializers, and RecordFilters.
 *
 * @param <T> Interface/Type of plugin we want to create instances of.
 */
public class PluginFactory<T> {
    /**
     * Manages access to files.
     */
    private final FileManager fileManager;

    /**
     * Type/Interface of class we want to create instances of.
     */
    private final Class<T> typeParameterClass;

    /**
     * Type of file.
     */
    private final FileType fileType;

    /**
     * Constructor.
     * @param typeParameterClass The type/interface of classes we can create instances of.
     */
    public PluginFactory(final FileType fileType, final Class<T> typeParameterClass, final FileManager fileManager) {
        this.fileType = Objects.requireNonNull(fileType);
        this.typeParameterClass = Objects.requireNonNull(typeParameterClass);
        this.fileManager = Objects.requireNonNull(fileManager);
    }

    /**
     * Get the Class instance of the plugin at the given classpath loaded using the
     * standard JVM ClassLoader.  Used to load default deserializers.
     *
     * @param classpath Classpath to class to load.
     * @return Class instance of the given classpath.
     * @throws LoaderException When we run into issues loading the class.
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
            final Path absolutePath = getPathForJar(jarName);
            final URL jarUrl = absolutePath.toUri().toURL();
            final ClassLoader pluginClassLoader = new PluginClassLoader(jarUrl, getClass().getClassLoader());
            return getPluginClass(pluginClassLoader, classpath);
        } catch (final IOException exception) {
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
            return dClass.getDeclaredConstructor().newInstance();
        } catch (final NoClassDefFoundError e) {
            // Typically this happens if the uploaded JAR references some dependency that was
            // not package in the JAR.  Attempt to provide a useful error msg.
            final String errorMsg = e.getMessage()
                + " - Does your JAR include all of its required dependencies? "
                + "See https://github.com/SourceLabOrg/kafka-webview-examples#packaging-a-jar";
            throw new LoaderException(errorMsg, e);
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new LoaderException(e.getMessage(), e);
        } catch (final NoSuchMethodException | InvocationTargetException e) {
            // Typically this happens if referenced class in the uploaded JAR has no default constructor.
            final String errorMsg = e.getMessage()
                + " - Does your class contain a default no argument constructor?";
            throw new LoaderException(errorMsg, e);
        }
    }
    
    /**
     * Check if instance of the class given at the classpath can be load from the given Jar.
     * @param jarName Jar to load the class from
     * @param classpath Classpath to class.
     * @return boolean true on success.
     * @throws LoaderException LoaderException When we run into issues.
     */
    public boolean checkPlugin(final String jarName, final String classpath) throws LoaderException {
        try {
            final Path absolutePath = getPathForJar(jarName);
            final URL jarUrl = absolutePath.toUri().toURL();
            // Windows issue, URLClassLoader open file so if we need to delete them 
            // (that the case for new uploaded file) then the close must be explicitly call.
            // More information available here:
            // https://docs.oracle.com/javase/8/docs/technotes/guides/net/ClassLoader.html
            try (URLClassLoader pluginClassLoader = new PluginClassLoader(jarUrl, getClass().getClassLoader())) {
                Class<? extends T> pluginClass = getPluginClass(pluginClassLoader, classpath);
                pluginClass.getDeclaredConstructor().newInstance();

                return true;
            }
        } catch (final IOException exception) {
            throw new LoaderException("Unable to load jar " + jarName, exception);
        } catch (final NoClassDefFoundError e) {
            // Typically this happens if the uploaded JAR references some dependency that was
            // not package in the JAR.  Attempt to provide a useful error msg.
            final String errorMsg = e.getMessage()
                + " - Does your JAR include all of its required dependencies? "
                + "See https://github.com/SourceLabOrg/kafka-webview-examples#packaging-a-jar";
            throw new LoaderException(errorMsg, e);
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new LoaderException(e.getMessage(), e);
        } catch (final NoSuchMethodException | InvocationTargetException e) {
            // Typically this happens if referenced class in the uploaded JAR has no default constructor.
            final String errorMsg = e.getMessage()
                + " - Does your class contain a default no argument constructor?";
            throw new LoaderException(errorMsg, e);
        }
    }

    /**
     * Get the full path on disk to the given Jar file.
     * @param jarName Jar to lookup full path to.
     */
    public Path getPathForJar(final String jarName) throws IOException {
        return fileManager.getFile(jarName, fileType);
    }
}
