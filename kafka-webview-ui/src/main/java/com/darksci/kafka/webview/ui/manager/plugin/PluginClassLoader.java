package com.darksci.kafka.webview.ui.manager.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;

/**
 * Marker or Wrapper around URLClassLoader so we can more easily determine what instances were
 * loaded by us, as well as define a restrictive permission set.
 */
public class PluginClassLoader extends URLClassLoader {
    /**
     * Constructor.
     * @param jarFileUrl Url to jar we want to load a class from
     * @param parent The parent class loader.
     */
    public PluginClassLoader(final URL jarFileUrl, final ClassLoader parent) {
        super(new URL[] {jarFileUrl}, parent);
    }

    /**
     * If a SecurityManager is in place, this will enforce a restrictive permission set.
     */
    @Override
    protected PermissionCollection getPermissions(final CodeSource codesource) {
        final PermissionCollection permissionCollection = super.getPermissions(codesource);
        return permissionCollection;
    }
}
