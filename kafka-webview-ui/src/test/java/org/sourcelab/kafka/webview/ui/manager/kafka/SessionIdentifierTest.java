package org.sourcelab.kafka.webview.ui.manager.kafka;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SessionIdentifierTest {

    /**
     * Basic validation over equals.
     */
    @Test
    public void testEquals() {
        final SessionIdentifier id1 = new SessionIdentifier(12L, "MySession");
        final SessionIdentifier id2 = new SessionIdentifier(12L, "MySession");
        final SessionIdentifier id3 = new SessionIdentifier(12L, "YourSession");
        final SessionIdentifier id4 = new SessionIdentifier(13L, "MySession");

        assertTrue("Should be equal", id1.equals(id1));
        assertTrue("Should be equal", id1.equals(id2));
        assertFalse("Should NOT be equal", id1.equals(null));
        assertFalse("Should NOT be equal", id1.equals(id3));
        assertFalse("Should NOT be equal", id1.equals(id4));
        assertFalse("Should NOT be equal", id3.equals(id4));
    }

    /**
     * Value object test.
     */
    @Test
    public void testGetters() {
        final SessionIdentifier id1 = new SessionIdentifier(12L, "MySession");
        assertEquals(12L, id1.getUserId());
        assertEquals("MySession", id1.getSessionId());
    }
}