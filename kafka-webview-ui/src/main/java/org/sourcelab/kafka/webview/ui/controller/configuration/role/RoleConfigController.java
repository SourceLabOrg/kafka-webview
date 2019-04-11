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

package org.sourcelab.kafka.webview.ui.controller.configuration.role;

import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.configuration.role.forms.RoleForm;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.manager.user.DuplicateRoleException;
import org.sourcelab.kafka.webview.ui.manager.user.RoleManager;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.manager.user.permission.RequirePermission;
import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.repository.RoleRepository;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Role entity CRUD.
 */
@Controller
@RequestMapping("/configuration/role")
public class RoleConfigController extends BaseController {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleManager roleManager;

    /**
     * Constructor.
     * @param roleRepository Repository for roles.
     * @param userRepository Repository for users.
     * @param roleManager Manager for interacting with Roles.
     */
    @Autowired
    public RoleConfigController(
        final RoleRepository roleRepository,
        final UserRepository userRepository,
        final RoleManager roleManager) {

        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.roleManager = roleManager;
    }

    /**
     * GET Displays main user index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    @RequirePermission(Permissions.ROLE_READ)
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all roles
        final Iterable<Role> rolesList = roleRepository.findAllByOrderByNameAsc();

        // Counts for all roles.
        final Map<Long, Long> usages = new HashMap<>();

        // Set defaults to 0
        rolesList.forEach((role) -> {
            usages.put(role.getId(), 0L);
        });

        // Override with actual usages.
        userRepository
            .getRoleCounts()
            .forEach((value) -> {
                usages.put((Long) value[0], (Long) value[1]);
            });

        model.addAttribute("roles", rolesList);
        model.addAttribute("roleUsageMap", Collections.unmodifiableMap(usages));

        return "configuration/role/index";
    }

    /**
     * GET Displays create role form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    @RequirePermission(Permissions.ROLE_CREATE)
    public String createRole(final RoleForm roleForm, final Model model, final RedirectAttributes redirectAttributes) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", "/configuration/role/create");
        model.addAttribute("permissionGroups", roleManager.getDefaultPermissionGroups());

        return "configuration/role/create";
    }

    /**
     * GET Displays edit role form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    @RequirePermission(Permissions.ROLE_MODIFY)
    public String editRoleForm(
        @PathVariable final Long id,
        final RoleForm roleForm,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Retrieve by id
        final Optional<Role> roleOptional = roleRepository.findById(id);

        // If we couldn't find the role
        if (!roleOptional.isPresent()) {
            // redirect
            // Set flash message
            final FlashMessage flashMessage = FlashMessage.newWarning("Unable to find role!");
            redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

            // redirect to cluster index
            return "redirect:/configuration/role";
        }
        final Role role = roleOptional.get();

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Edit: " + role.getName(), null);
        model.addAttribute("permissionGroups", roleManager.getDefaultPermissionGroups());

        // Build form
        roleForm.setId(role.getId());
        roleForm.setName(role.getName());
        roleForm.setPermissions(roleManager.getPermissionsForRole(role.getId()));

        // Display template
        return "configuration/role/create";
    }

    /**
     * Handles Creating new roles.
     */
    @RequestMapping(path = "/create", method = RequestMethod.POST)
    @RequirePermission(Permissions.ROLE_CREATE)
    @Transactional
    public String filterCreate(
        @Valid final RoleForm roleForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        final Model model,
        HttpServletResponse response) throws IOException {

        final boolean updateExisting = roleForm.exists();
        if (updateExisting) {
            // This means they hit this end point with a role Id, which would be interpreted as an
            // update existing role.  This end point shouldn't handle those requests.
            response.sendError(HttpStatus.BAD_REQUEST.value());
            return null;
        }
        return handleUpdateRole(roleForm, bindingResult, redirectAttributes, model);
    }

    /**
     * Handles updating existing roles.
     */
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    @RequirePermission(Permissions.ROLE_MODIFY)
    @Transactional
    public String filterUpdate(
        @Valid final RoleForm roleForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        final Model model,
        HttpServletResponse response) throws IOException {

        final boolean updateExisting = roleForm.exists();
        if (!updateExisting) {
            // This means they hit this end point without a role Id, which would be interpreted as
            // creating a new role.  This end point shouldn't handle those requests.
            response.sendError(HttpStatus.BAD_REQUEST.value());
            return null;
        }
        return handleUpdateRole(roleForm, bindingResult, redirectAttributes, model);
    }

    /**
     * POST updates a role.
     */
    private String handleUpdateRole(
        @Valid final RoleForm roleForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Validate role name doesn't already exist!
        final Role existingRole = roleRepository.findByName(roleForm.getName());
        if ((roleForm.exists() && existingRole != null && existingRole.getId() != roleForm.getId())
            || (!roleForm.exists() && existingRole != null)) {
            bindingResult.addError(new FieldError(
                "userRole",
                "name",
                roleForm.getName(),
                true,
                null,
                null,
                "Name is already used")
            );
        }

        // If the role already exists AND we're editing the role of the current user
        if (roleForm.exists() && roleForm.getId().equals(getLoggedInUser().getUserModel().getRoleId()) ) {
            final Collection<Permissions> setPermissions = roleForm.getPermissions();
            // We don't want to allow them to remove the ROLE_MODIFY and ROLE_READ permissions
            if (!setPermissions.contains(Permissions.ROLE_MODIFY) || !setPermissions.contains(Permissions.ROLE_READ)) {
                // Block them from revoking privileges to modify roles
                bindingResult.addError(new FieldError(
                    "userRole",
                    "permissions",
                    Permissions.ROLE_MODIFY,
                    true,
                    null,
                    null,
                    "You may not remove the permissions Role:Update and/or Role:Read from your current user's role.")
                );
            }
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            return createRole(roleForm, model, redirectAttributes);
        }

        // The underlying role entity.
        Role roleEntity = null;

        if (!roleForm.exists()) {
            // Create the role
            try {
                roleEntity = roleManager.createNewRole(roleForm.getName());

                // Add success flash msg
                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newSuccess("Created new role " + roleEntity.getName() + "!"));
            } catch (final DuplicateRoleException duplicateRoleException) {
                // Add error flash msg
                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newWarning("Error creating new role! " + duplicateRoleException.getMessage()));
            }
        } else {
            // Update existing role
            final Optional<Role> roleOptional = roleRepository.findById(roleForm.getId());

            // If the role doesn't exist
            if (!roleOptional.isPresent()) {
                // Add error flash msg
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Error creating new role!"));
            } else {
                roleEntity = roleOptional.get();

                // Update role
                roleEntity.setName(roleForm.getName());

                // Update
                roleRepository.save(roleEntity);

                // Add success flash msg
                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newSuccess("Updated role " + roleEntity.getName() + "!"));
            }
        }

        // If we didn't have an error
        if (roleEntity != null) {
            // Update permissions on that role.
            roleManager.updatePermissions(roleEntity.getId(), roleForm.getPermissions());
        }

        return "redirect:/configuration/role";
    }

    /**
     * POST copies the selected role.
     */
    @RequestMapping(path = "/copy/{id}", method = RequestMethod.POST)
    @Transactional
    @RequirePermission(Permissions.ROLE_CREATE)
    public String copyRole(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        if (!roleRepository.existsById(id)) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find role!"));
        } else {
            // Retrieve view
            roleRepository.findById(id).ifPresent((role) -> {
                // Create Copy manager
                try {
                    roleManager.copyRole(role, "Copy of " + role.getName());
                    redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Copied role!"));
                } catch (final DuplicateRoleException duplicateRoleException) {
                    // Add error flash msg
                    redirectAttributes.addFlashAttribute(
                        "FlashMessage",
                        FlashMessage.newWarning("Error creating new role! " + duplicateRoleException.getMessage()));
                }
            });
        }

        // redirect to role index
        return "redirect:/configuration/role";
    }

    /**
     * POST deletes the selected role.
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    @Transactional
    @RequirePermission(Permissions.ROLE_DELETE)
    public String deleteRole(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        if (!roleRepository.existsById(id)) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find role!"));
        } else {
            // Delete it
            if (roleManager.deleteRole(id)) {
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Deleted role!"));
            } else {
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Role in use! Unable to delete!"));
            }
        }

        // redirect to role index
        return "redirect:/configuration/role";
    }

    /**
     * Helper for setting up BreadCrumbs for User actions.
     */
    private void setupBreadCrumbs(final Model model, final String name, final String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
            .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb("Roles", "/configuration/role");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Roles", null);
        }
    }
}
