package com.darksci.kafkaview.manager.plugin;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Marker or Wrapper around URLClassLoader so we can easily determine what instances were
 * loaded by us.
 */
public class PluginClassLoader extends URLClassLoader {
    /**
     * Constructor.
     * @param jarFileUrl Url to jar we want to load a class from
     * @param parent The parent class loader.
     */
    public PluginClassLoader(URL jarFileUrl, ClassLoader parent) {
        super(new URL[] {jarFileUrl}, parent);
    }

    public PluginClassLoader(final URL jarFileUrl) {
        super(new URL[] {jarFileUrl});
    }
}
