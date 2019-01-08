/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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
        final SessionIdentifier id1 = new SessionIdentifier(12L, "MySession", SessionIdentifier.Context.WEB);
        final SessionIdentifier id2 = new SessionIdentifier(12L, "MySession", SessionIdentifier.Context.WEB);
        final SessionIdentifier id3 = new SessionIdentifier(12L, "YourSession", SessionIdentifier.Context.WEB);
        final SessionIdentifier id4 = new SessionIdentifier(13L, "MySession", SessionIdentifier.Context.WEB);
        final SessionIdentifier id5 = new SessionIdentifier(13L, "MySession", SessionIdentifier.Context.STREAM);

        assertTrue("Should be equal", id1.equals(id1));
        assertTrue("Should be equal", id1.equals(id2));
        assertFalse("Should NOT be equal", id1.equals(null));
        assertFalse("Should NOT be equal", id1.equals(id3));
        assertFalse("Should NOT be equal", id1.equals(id4));
        assertFalse("Should NOT be equal", id3.equals(id4));
        assertFalse("Should NOT be equal", id4.equals(id5));
    }

    /**
     * Value object test.
     */
    @Test
    public void testGetters() {
        final SessionIdentifier id1 = new SessionIdentifier(12L, "MySession", SessionIdentifier.Context.WEB);
        assertEquals(12L, id1.getUserId());
        assertEquals("MySession", id1.getSessionId());
        assertEquals(SessionIdentifier.Context.WEB, id1.getContext());
    }
}