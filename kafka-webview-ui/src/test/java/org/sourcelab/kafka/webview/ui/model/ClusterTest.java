package org.sourcelab.kafka.webview.ui.model;

import org.junit.Test;
import org.sourcelab.kafka.webview.ui.controller.configuration.user.forms.UserForm;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;


public class ClusterTest {

    /**
     * Validate toString never spits out sensitive fields
     */
    @Test
    public void testToString() throws IllegalAccessException, NoSuchFieldException {
        final String expectedSecret1 = "MySuperSecretKey";
        final String expectedSecret2 = "AnotherSecret";

        // Create app Properties instance
        final Cluster cluster = new Cluster();

        // Jump through hoops to set properties
        final Field field1 = cluster.getClass().getDeclaredField("trustStorePassword");
        field1.setAccessible(true);
        field1.set(cluster, expectedSecret1);

        final Field field2 = cluster.getClass().getDeclaredField("keyStorePassword");
        field2.setAccessible(true);
        field2.set(cluster, expectedSecret1);

        final String result = cluster.toString();
        assertFalse("Should not contain our sensitive field", result.contains(expectedSecret1));
        assertFalse("Should not contain our sensitive field", result.contains(expectedSecret2));
    }
}