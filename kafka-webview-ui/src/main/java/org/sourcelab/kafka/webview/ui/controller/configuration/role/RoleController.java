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
import org.sourcelab.kafka.webview.ui.controller.configuration.user.forms.UserForm;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.model.Role;
import org.sourcelab.kafka.webview.ui.repository.RoleRepository;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Role entity CRUD.
 */
@Controller
@RequestMapping("/configuration/role")
public class RoleController extends BaseController {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /**
     * Constructor.
     * @param roleRepository Repository for roles.
     * @param userRepository Repository for users.
     */
    @Autowired
    public RoleController(
        final RoleRepository roleRepository,
        final UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    /**
     * GET Displays main user index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
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
    public String createRole(final RoleForm roleForm, final Model model, final RedirectAttributes redirectAttributes) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", "/configuration/role/create");

        return "configuration/role/create";
    }

    /**
     * GET Displays edit role form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editRoleForm(
        @PathVariable final Long id,
        final RoleForm roleForm,
        final RedirectAttributes redirectAttributes,
        final Model model) {


        // Display template
        return "configuration/role/create";
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
