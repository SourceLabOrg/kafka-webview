package org.sourcelab.kafka.webview.ui.manager.user.permission;

import java.util.Collection;

public class PermissionGroup {
    private final String name;
    private final String description;
    private final Collection<Permissions> permissions;

    public PermissionGroup(
        final String name,
        final String description,
        final Collection<Permissions> permissions
    ) {
        this.name = name;
        this.description = description;
        this.permissions = permissions;
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

    public boolean doesContainPermission(final Permissions permissions) {
        return getPermissions().contains(permissions);
    }
}
