package org.sourcelab.kafka.webview.ui.configuration;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;

public class AppPropertiesTest {

    /**
     * Validate toString never spits out sensitive fields.
     */
    @Test
    public void testToString() throws NoSuchFieldException, IllegalAccessException {
        final String expectedSecret = "MySuperSecretKey";

        // Create app Properties instance
        final AppProperties appProperties = new AppProperties();

        // Jump through hoops to set property
        final Field field = appProperties.getClass().getDeclaredField("appKey");
        field.setAccessible(true);
        field.set(appProperties, expectedSecret);

        final String result = appProperties.toString();
        assertFalse("Should not contain our sensitive field", result.contains(expectedSecret));
    }
}