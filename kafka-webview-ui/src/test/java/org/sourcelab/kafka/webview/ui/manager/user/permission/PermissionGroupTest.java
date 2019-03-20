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

package org.sourcelab.kafka.webview.ui.manager.user.permission;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class PermissionGroupTest {

    /**
     * Quick n' Dirty sanity tests.
     */
    @Test
    public void testSanityTest() {
        final String name = "My Group Name";
        final String description = "My description";
        final List<Permissions> expectedPermissions = new ArrayList<>();
        expectedPermissions.add(Permissions.TOPIC_CREATE);
        expectedPermissions.add(Permissions.TOPIC_MODIFY);
        expectedPermissions.add(Permissions.USER_DELETE);

        final PermissionGroup group = new PermissionGroup(
            name,
            description,
            expectedPermissions
        );

        assertEquals(name, group.getName());
        assertEquals(description, group.getDescription());
        assertNotNull(group.getPermissions());

        final Collection<Permissions> permissions = group.getPermissions();
        assertEquals(expectedPermissions.size(), permissions.size());
        assertTrue(permissions.containsAll(expectedPermissions));

        assertTrue("Should contain permission", group.doesContainPermission(Permissions.TOPIC_CREATE));
        assertTrue("Should contain permission", group.doesContainPermission(Permissions.TOPIC_MODIFY));
        assertTrue("Should contain permission", group.doesContainPermission(Permissions.USER_DELETE));

        // Should not contain these
        assertFalse("Should contain permission", group.doesContainPermission(Permissions.CLUSTER_CREATE));
    }

    /**
     * Quick n' Dirty sanity tests.
     */
    @Test
    public void testMakesSetOfPermissionsUnique() {
        final String name = "My Group Name";
        final String description = "My description";
        final List<Permissions> expectedPermissions = new ArrayList<>();
        expectedPermissions.add(Permissions.TOPIC_CREATE);
        expectedPermissions.add(Permissions.TOPIC_CREATE);

        final PermissionGroup group = new PermissionGroup(
            name,
            description,
            expectedPermissions
        );

        assertNotNull(group.getPermissions());

        final Collection<Permissions> permissions = group.getPermissions();
        assertEquals(1, permissions.size());
        assertTrue(permissions.containsAll(expectedPermissions));

        assertTrue("Should contain permission", group.doesContainPermission(Permissions.TOPIC_CREATE));
    }
}