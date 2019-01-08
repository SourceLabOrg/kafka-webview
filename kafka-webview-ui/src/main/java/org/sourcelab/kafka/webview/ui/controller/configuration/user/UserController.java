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

package org.sourcelab.kafka.webview.ui.controller.configuration.user;

import org.sourcelab.kafka.webview.ui.configuration.AppProperties;
import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.configuration.user.forms.UserForm;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.manager.user.UserManager;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.UserRole;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for User entity CRUD.
 */
@Controller
@RequestMapping("/configuration/user")
public class UserController extends BaseController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserManager userManager;

    @Autowired
    private AppProperties appProperties;

    /**
     * GET Displays main user index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final UserForm userForm, final Model model, final RedirectAttributes redirectAttributes) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Check for LDAP auth method and restrict access.
        if (redirectIfUsingLdapAuthentication(redirectAttributes)) {
            return "redirect:/";
        }

        // Retrieve all users
        final Iterable<User> usersList = userRepository.findAllByIsActiveOrderByEmailAsc(true);
        model.addAttribute("users", usersList);

        return "configuration/user/index";
    }

    /**
     * GET Displays create user form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createUser(final UserForm userForm, final Model model, final RedirectAttributes redirectAttributes) {
        // Check for LDAP auth method and restrict access.
        if (redirectIfUsingLdapAuthentication(redirectAttributes)) {
            return "redirect:/";
        }

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", "/configuration/user/create");

        // Set isAdmin attribute
        model.addAttribute("isAdmin", hasRole("ADMIN"));
        model.addAttribute("userRoles", getUserRoleOptions());

        return "configuration/user/create";
    }

    private List<UserRole> getUserRoleOptions() {
        final List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(UserRole.ROLE_USER);
        userRoles.add(UserRole.ROLE_ADMIN);
        return userRoles;
    }

    /**
     * GET Displays edit cluster form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editUserForm(
        @PathVariable final Long id,
        final UserForm userForm,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        final boolean isAdmin = hasRole("ADMIN");

        // If user doesn't have admin role, and Id isn't their own
        if (!isAdmin && !id.equals(getLoggedInUserId())) {
            // Cant edit this user.
            return "redirect:/";
        }

        // Check for LDAP auth method and restrict access.
        if (redirectIfUsingLdapAuthentication(redirectAttributes)) {
            return "redirect:/";
        }

        // Set isAdmin attribute
        model.addAttribute("isAdmin", isAdmin);

        // Retrieve by id
        final Optional<User> userOptional = userRepository.findById(id);

        // If we couldn't find the user, or the user is archived.
        if (!userOptional.isPresent() || !userOptional.get().getActive()) {
            // redirect
            // Set flash message
            final FlashMessage flashMessage = FlashMessage.newWarning("Unable to find user!");
            redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

            // redirect to cluster index
            return "redirect:/configuration/user";
        }
        final User user = userOptional.get();

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Edit: " + user.getDisplayName(), null);

        // Build form
        userForm.setId(user.getId());
        userForm.setEmail(user.getEmail());
        userForm.setDisplayName(user.getDisplayName());
        userForm.setUserRole(user.getRole());

        // Set user role options.
        model.addAttribute("userRoles", getUserRoleOptions());

        // Display template
        return "configuration/user/create";
    }

    /**
     * POST updates a user.
     */
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public String update(
        @Valid final UserForm userForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        final boolean isAdmin = hasRole("ADMIN");

        // If user doesn't have admin role, and Id isn't their own
        if (!isAdmin && !userForm.getId().equals(getLoggedInUserId())) {
            // Can't modify this user.
            return "redirect:/";
        }

        // Check for LDAP auth method and restrict access.
        if (redirectIfUsingLdapAuthentication(redirectAttributes)) {
            return "redirect:/";
        }

        // Set isAdmin attribute
        model.addAttribute("isAdmin", isAdmin);

        // Validate email doesn't already exist!
        final User existingUser = userRepository.findByEmail(userForm.getEmail());
        if ((userForm.exists() && existingUser != null && existingUser.getId() != userForm.getId())
            || (!userForm.exists() && existingUser != null)) {
            bindingResult.addError(new FieldError(
                "userForm",
                "email",
                userForm.getEmail(),
                true,
                null,
                null,
                "Email is already used")
            );
        }

        if (!userForm.exists() || !userForm.getPassword().isEmpty()) {
            if (userForm.getPassword().length() < 8) {
                bindingResult.addError(new FieldError(
                    "userForm",
                    "password",
                    userForm.getPassword(),
                    true,
                    null,
                    null,
                    "Please enter a password of at least 8 characters")
                );
            }
        }

        // For new users, or if password is set
        if (!userForm.exists() || (userForm.exists() && !userForm.getPassword().isEmpty())) {
            // Validate password == password2
            if (!userForm.getPassword().equals(userForm.getPassword2())) {
                bindingResult.addError(new FieldError(
                    "userForm",
                    "password",
                    userForm.getPassword(),
                    true,
                    null,
                    null,
                    "Passwords do not match")
                );
                bindingResult.addError(new FieldError(
                    "userForm",
                    "password2",
                    userForm.getPassword(),
                    true,
                    null,
                    null,
                    "Passwords do not match")
                );
            }
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            return createUser(userForm, model, redirectAttributes);
        }

        if (!userForm.exists()) {
            // Create the user
            final User newUser = userManager.createNewUser(
                userForm.getEmail(),
                userForm.getDisplayName(),
                userForm.getPassword(),
                userForm.getUserRole()
            );

            if (newUser == null) {
                // Add error flash msg
                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newWarning("Error creating new user!"));
            } else {
                // Add success flash msg
                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newSuccess("Created new user " + newUser.getDisplayName() + "!"));
            }
        } else {
            // Update existing user
            final Optional<User> userOptional = userRepository.findById(userForm.getId());

            // If the user is archived
            if (!userOptional.isPresent() || !userOptional.get().getActive()) {
                // Add error flash msg
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Error creating new user!"));
            } else {
                final User user = userOptional.get();

                // Update user
                user.setEmail(userForm.getEmail());
                user.setDisplayName(userForm.getDisplayName());

                // Only admins can set the role
                if (hasRole("ADMIN")) {
                    user.setRole(userForm.getUserRole());
                }

                // If they changed their password
                if (!userForm.getPassword().isEmpty()) {
                    user.setPassword(userManager.encodePassword(userForm.getPassword()));
                }

                // Update
                userRepository.save(user);

                // Add success flash msg
                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newSuccess("Updated user " + user.getDisplayName() + "!"));
            }
        }

        // Otherwise success
        if (isAdmin) {
            return "redirect:/configuration/user";
        } else {
            return "redirect:/";
        }
    }

    /**
     * POST deletes the selected user.
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String delete(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        // Check for LDAP auth method and restrict access.
        if (redirectIfUsingLdapAuthentication(redirectAttributes)) {
            return "redirect:/";
        }

        // Retrieve it
        final Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find user!"));
        } else if (userOptional.get().getId() == getLoggedInUserId()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to delete your own user!"));
        } else {
            final User user = userOptional.get();

            // Rename user
            user.setEmail("DELETED: " + user.getEmail());
            user.setActive(false);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Archived user!"));
        }

        // redirect to cluster index
        return "redirect:/configuration/user";
    }

    /**
     * Helper for setting up BreadCrumbs for User actions.
     */
    private void setupBreadCrumbs(final Model model, final String name, final String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
            .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb("Users", "/configuration/user");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Users", null);
        }
    }

    /**
     * If app is configured to use LDAP authentication, restrict access to User configuration.
     * @param redirectAttributes for applying flash messages.
     * @return true if we're using ldap and should restrict access, false if not.
     */
    private boolean redirectIfUsingLdapAuthentication(final RedirectAttributes redirectAttributes) {
        // If user auth is disabled.
        if (!appProperties.isUserAuthEnabled()) {
            return false;
        }

        if (!appProperties.getLdapProperties().isEnabled()) {
            return false;
        }

        final FlashMessage flashMessage = FlashMessage.newWarning("User management disabled when using LDAP authentication.");
        redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);
        return true;
    }
}
