package org.sourcelab.kafka.webview.ui.tools;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Test Utility for interacting with testPlugins.jar files.
 */
public class TestPluginJars {
    private static final String jar1_0_0 = "testDeserializer/1.0.0/testPlugins.jar";
    private static final String jar1_1_0 = "testDeserializer/1.1.0/testPlugins.jar";

    public static TestJar getTestDeserializerForVersion1_0_0() {
        return new TestJar(
            jar1_0_0,
            "examples.deserializer.ExampleDeserializer"
        );
    }

    public static TestJar getTestLowOffsetFilterForVersion1_0_0() {
        return new TestJar(
            jar1_0_0,
            "examples.filter.LowOffsetFilter"
        );
    }

    public static TestJar getTestDeserializerForVersion1_1_0() {
        return new TestJar(
            jar1_1_0,
            "examples.deserializer.ExampleDeserializer"
        );
    }

    public static TestJar getTestLowOffsetFilterForVersion1_1_0() {
        return new TestJar(
            jar1_1_0,
            "examples.filter.LowOffsetFilter"
        );
    }

    /**
     * Holder for TestJar details.
     */
    public static class TestJar
    {
        private final String jarPath;
        private final String clazz;


        public TestJar(final String jarPath, final String clazz) {
            this.jarPath = jarPath;
            this.clazz = clazz;
        }

        public URL getJarUrl() {
            return getClass().getClassLoader().getResource(jarPath);
        }

        public String getClazz() {
            return clazz;
        }

        public InputStream getInputStream() {
            return getClass().getClassLoader().getResourceAsStream(jarPath);
        }

        public String getFilename() {
            return Paths.get(jarPath).getFileName().toString();
        }
    }
}
