package com.darksci.kafka.webview.ui.manager.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class NewClassLoader extends URLClassLoader
{
    private File file;
    private List dependencies;

    public NewClassLoader (File file) throws IOException {
        super(new URL[] {file.toURL()});

        this.file = file;
        this.dependencies = extractDependencies(file);
    }

    public NewClassLoader(final URL[] urls) {
        super(urls);
    }

    public static NewClassLoader create(File jar) throws IOException {
        final URL[] urls = extractDependencies(jar).toArray(new URL[0]);
        return new NewClassLoader(urls);
    }

    @Override
    public String toString () {
        return file.toString();
    }

    private static List<URL> extractDependencies (final File file) throws IOException {
        JarFile jar = new JarFile(file);
        Manifest man = jar.getManifest();
        Attributes attr = man.getMainAttributes();

        List<URL> l = new ArrayList();
        l.add(file.toURL());
        String str = attr.getValue("Class-Path");
        if (str != null) {
            StringTokenizer tok = new StringTokenizer(str);
            while (tok.hasMoreTokens()) {
                final String jarPath = "jar:file:" + file.getAbsolutePath() + "!/" + tok.nextToken();
                final File depJar = new File(jarPath);
                //final File depJar = new File(file.getParentFile(), tok.nextToken());
                l.add(new URL(jarPath));
            }
        }

        return l;
    }

//    public Class loadClass (String name, boolean resolve) throws ClassNotFoundException {
//        // Try to load the class from our JAR.
//        try {
//            return loadClassForComponent(name, resolve);
//        } catch (ClassNotFoundException e) {
//            return super.loadClass(name, resolve);
//        }
//    }
//
//    public Class loadClassForComponent (String name, boolean resolve) throws ClassNotFoundException {
//        Class c = findLoadedClass(name);
//
//        // Even if findLoadedClass returns a real class, we might simply
//        // be its initiating ClassLoader.  Only return it if we're actually
//        // its defining ClassLoader (as determined by Class.getClassLoader).
//        if (c == null || c.getClassLoader() != this) {
//            c = findClass(name);
//
//            if (resolve) {
//                resolveClass(c);
//            }
//        }
//        return c;
//    }
//
//    public URL findResource (String name) {
//        // Try to load the resource from our JAR.
//        URL url = getResourceForComponent(name);
//        if (url != null) {
//            return url;
//        }
//        return null;
//    }
//
//    public URL getResourceForComponent (String name) {
//        return super.findResource(name);
//    }
//
//    public Enumeration findResources (String name) throws IOException {
//        Vector vec = new Vector();
//
//        // Try to load the resource from our JAR.
//        Enumeration e = getResourcesForComponent(name);
//        while (e.hasMoreElements()) {
//            vec.add(e.nextElement());
//        }
//        return vec.elements();
//    }
//
//    public Enumeration getResourcesForComponent (String name) throws IOException {
//        try {
//            return super.findResources(name);
//        } catch (IOException ex) {
//            return new Vector().elements();
//        }
//    }
}
