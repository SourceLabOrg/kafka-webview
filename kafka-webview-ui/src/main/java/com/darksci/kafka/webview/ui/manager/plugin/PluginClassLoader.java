package com.darksci.kafka.webview.ui.manager.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;

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

    @Override
    protected PermissionCollection getPermissions(CodeSource codesource) {
        final PermissionCollection permissionCollection = super.getPermissions(codesource);
//        permissionCollection.add(new ReflectPermission("suppressAccessChecks"));
        return permissionCollection;
    }
}
