package org.sourcelab.kafka.webview.ui.manager.user;

import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Logic for creating/updating roles.
 */
@Component
public class RoleManager {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleManager(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Create a new role with required entities.
     * @param name Name of the role.
     * @return Role instance.
     */
    public Role createNewRole(final String name) {
        final Role role = new Role();
        role.setName(name);
        roleRepository.save(role);

        return role;
    }
}
