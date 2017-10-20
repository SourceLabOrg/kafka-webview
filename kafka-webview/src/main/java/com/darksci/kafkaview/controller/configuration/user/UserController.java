package com.darksci.kafkaview.controller.configuration.user;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.controller.configuration.user.forms.UserForm;
import com.darksci.kafkaview.manager.ui.BreadCrumbManager;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.manager.user.UserManager;
import com.darksci.kafkaview.model.User;
import com.darksci.kafkaview.model.UserRole;
import com.darksci.kafkaview.repository.UserRepository;
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

@Controller
@RequestMapping("/configuration/user")
public class UserController extends BaseController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserManager userManager;

    /**
     * GET Displays main user index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final UserForm userForm, final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all users
        final Iterable<User> usersList = userRepository.findAll();
        model.addAttribute("users", usersList);

        return "configuration/user/index";
    }

    /**
     * GET Displays create user form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createUser(final UserForm userForm, final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", "/configuration/user/create");

        // Set user role options.
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
        final @PathVariable Long id,
        final UserForm userForm,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Retrieve by id
        final User user = userRepository.findOne(id);
        if (user == null) {
            // redirect
            // Set flash message
            final FlashMessage flashMessage = FlashMessage.newWarning("Unable to find user!");
            redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

            // redirect to cluster index
            return "redirect:/configuration/user";
        }

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
     * POST updates a user
     */
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public String update(
        @Valid final UserForm userForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Validate email doesn't already exist!
        final User existingUser = userRepository.findByEmail(userForm.getEmail());
        if ((userForm.exists() && existingUser != null && existingUser.getId() != userForm.getId()) ||
            (!userForm.exists() && existingUser != null)){
            bindingResult.addError(new FieldError(
                "userForm", "email", userForm.getEmail(), true, null, null, "Email is already used")
            );
        }

        if (!userForm.exists() || !userForm.getPassword().isEmpty()) {
            if (userForm.getPassword().length() < 8) {
                bindingResult.addError(new FieldError(
                    "userForm", "password", userForm.getPassword(), true, null, null, "Please enter a password of at least 8 characters"
                ));
            }
        }

        // For new users, or if password is set
        if (!userForm.exists() || (userForm.exists() && !userForm.getPassword().isEmpty())) {
            // Validate password == password2
            if (!userForm.getPassword().equals(userForm.getPassword2())) {
                bindingResult.addError(new FieldError(
                    "userForm", "password", userForm.getPassword(), true, null, null, "Passwords do not match")
                );
                bindingResult.addError(new FieldError(
                    "userForm", "password2", userForm.getPassword(), true, null, null, "Passwords do not match")
                );
            }
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            return createUser(userForm, model);
        }

        if (!userForm.exists()) {
            // Create the user
            final User newUser = userManager.createNewUser(userForm.getEmail(), userForm.getDisplayName(), userForm.getPassword(), userForm.getUserRole());

            if (newUser == null) {
                // Add error flash msg
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Error registering new user!"));
            } else {
                // Add success flash msg
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Created new user " + newUser.getDisplayName() + "!"));
            }
        } else {
            // Update existing user
            final User user = userRepository.findOne(userForm.getId());
            user.setEmail(userForm.getEmail());
            user.setDisplayName(userForm.getDisplayName());
            user.setRole(userForm.getUserRole());

            // If they changed their password
            if (!userForm.getPassword().isEmpty()) {
                user.setPassword(userManager.encodePassword(userForm.getPassword()));
            }

            // Update
            userRepository.save(user);

            // Add success flash msg
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Updated user " + user.getDisplayName() + "!"));
        }

        // Otherwise success
        return "redirect:/configuration/user";
    }

    private void setupBreadCrumbs(final Model model, String name, String url) {
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
}
