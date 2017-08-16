package com.dsc.wai.controller.register;

import com.dsc.wai.configuration.CustomUserDetails;
import com.dsc.wai.controller.BaseController;
import com.dsc.wai.controller.register.forms.RegisterForm;
import com.dsc.wai.manager.ui.FlashMessage;
import com.dsc.wai.manager.user.NewUserManager;
import com.dsc.wai.model.User;
import com.dsc.wai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * Handles registering a new user.
 */
@Controller
public class RegisterController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NewUserManager newUserManager;

    /**
     * GET Requests handle displaying new user registration form.
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String registerForm(final RegisterForm registerForm) {
        // Redirect to home if already logged in
        if (isLoggedIn()) {
            return "redirect:/";
        }

        return "register/registerForm";
    }

    /**
     * POST Requests handle submitting new user registration form.
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String registerFormSubmit(
        @Valid final RegisterForm registerForm,
        final BindingResult bindingResult,
        final Model model,
        final RedirectAttributes redirectAttributes
    ) {
        // Redirect to home if already logged in
        if (isLoggedIn()) {
            return "redirect:/";
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("FlashMessage", FlashMessage.newWarning("Please correct errors."));
            return "register/registerForm";
        }

        // Validate email doesn't already exist!
        final User existingUser = userRepository.findByEmail(registerForm.getEmail());
        if (existingUser != null) {
            model.addAttribute("FlashMessage", FlashMessage.newWarning("Email address already registered with a user!"));
            return "register/registerForm";
        }

        // Create the user
        final User newUser = newUserManager.createNewUser(registerForm.getEmail(), registerForm.getDisplayName(), registerForm.getPassword());
        if (newUser == null) {
            // Add error flash msg
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Error registering new user!"));
        } else {
            // Add success flash msg
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Welcome " + newUser.getDisplayName() + "!  Thanks for registering!"));

            // Log the user in
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                    new CustomUserDetails(newUser),
                    "N/A",
                    AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER")
                )
            );
        }

        // Otherwise success
        return "redirect:/";
    }
}
