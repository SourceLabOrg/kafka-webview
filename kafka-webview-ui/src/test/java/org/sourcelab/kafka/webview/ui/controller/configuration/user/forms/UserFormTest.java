package org.sourcelab.kafka.webview.ui.controller.configuration.user.forms;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class UserFormTest {

    /**
     * Validate toString never spits out sensitive fields
     */
    @Test
    public void testToString() throws IllegalAccessException, NoSuchFieldException {
        final String expectedSecret1 = "MySuperSecretKey";
        final String expectedSecret2 = "AnotherSecret";

        // Create app Properties instance
        final UserForm userForm = new UserForm();

        // Jump through hoops to set properties
        final Field field1 = userForm.getClass().getDeclaredField("password");
        field1.setAccessible(true);
        field1.set(userForm, expectedSecret1);

        final Field field2 = userForm.getClass().getDeclaredField("password2");
        field2.setAccessible(true);
        field2.set(userForm, expectedSecret1);

        final String result = userForm.toString();
        assertFalse("Should not contain our sensitive field", result.contains(expectedSecret1));
        assertFalse("Should not contain our sensitive field", result.contains(expectedSecret2));
    }
}