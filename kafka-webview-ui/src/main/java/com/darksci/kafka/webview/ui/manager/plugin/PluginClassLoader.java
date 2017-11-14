/**
 * MIT License
 *
 * Copyright (c) 2017 Stephen Powis https://github.com/Crim/kafka-webview
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
