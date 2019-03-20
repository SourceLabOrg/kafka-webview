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

/**
 * Represents permissions in the application based on functional area.
 */
public enum Permissions {
    // Cluster
    CLUSTER_READ("Read"),
    CLUSTER_CREATE("Create"),
    CLUSTER_MODIFY("Update"),
    CLUSTER_DELETE("Delete"),

    // View
    VIEW_READ("Read"),
    VIEW_CREATE("Create"),
    VIEW_MODIFY("Update"),
    VIEW_DELETE("Delete"),

    // Topic
    TOPIC_READ("Read"),
    TOPIC_CREATE("Create"),
    TOPIC_MODIFY("Update"),
    TOPIC_DELETE("Delete"),

    // Consumers
    CONSUMER_READ("Read"),
    CONSUMER_MODIFY("Update"),
    CONSUMER_DELETE("Delete"),

    // Users
    USER_READ("Read"),
    USER_CREATE("Create"),
    USER_MODIFY("Update"),
    USER_DELETE("Delete"),

    // Roles
    ROLE_READ("Read"),
    ROLE_CREATE("Create"),
    ROLE_MODIFY("Update"),
    ROLE_DELETE("Delete");

    private final String displayName;

    Permissions(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the user friendly display name for the permission.
     * @return user friendly display name for the permission.
     */
    public String getDisplayName() {
        return displayName;
    }
}
