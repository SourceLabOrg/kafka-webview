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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Defines a group of related permissions.
 */
public class PermissionGroup {
    private final String name;
    private final String description;
    private final Collection<Permissions> permissions;

    /**
     * Constructor.
     * @param name Name of the group.
     * @param description Description of the group.
     * @param permissions Collection of permissions contained by the group.
     */
    public PermissionGroup(
        final String name,
        final String description,
        final Collection<Permissions> permissions
    ) {
        this.name = name;
        this.description = description;

        // Use set to ensure unique values.
        this.permissions = Collections.unmodifiableSet(new HashSet<>(permissions));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Collection<Permissions> getPermissions() {
        return permissions;
    }

    /**
     * Given a permission enum value, is that value contained within the group?
     * @param permissions The permission enum to determine if it's contained within the set.
     * @return true if so, false if not contained.
     */
    public boolean doesContainPermission(final Permissions permissions) {
        return getPermissions().contains(permissions);
    }
}
