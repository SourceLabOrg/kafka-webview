package com.darksci.kafka.webview.controller.login;

import com.darksci.kafka.webview.controller.BaseController;
import com.darksci.kafka.webview.manager.user.CustomUserDetails;
import com.darksci.kafka.webview.controller.login.forms.LostPasswordForm;
import com.darksci.kafka.webview.controller.login.forms.ResetPasswordForm;
import com.darksci.kafka.webview.manager.ui.FlashMessage;
import com.darksci.kafka.webview.model.User;
import com.darksci.kafka.webview.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * For handling logins.
 */
@Controller
public class LoginController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * GET Displays the Login Form.
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String loginForm(final Model model, final @RequestParam(value = "error", required = false) String isError) {
        // Redirect to home
        if (isLoggedIn()) {
            return "redirect:/";
        }

        // If we don't have an error
        if (isError != null) {
            // Display error string
            model.addAttribute("FlashMessage", FlashMessage.newWarning("Invalid Username or Password!"));
        }

        return "login.html";
    }

    /**
     * GET Displays the Lost Password Form.
     */
    @RequestMapping(path = "/login/lostPassword", method = RequestMethod.GET)
    public String lostPasswordForm(final LostPasswordForm lostPasswordForm) {
        // Redirect to home if already logged in
        if (isLoggedIn()) {
            return "redirect:/";
        }

        return "login/lostPasswordForm";
    }

    /**
     * POST Requests handle submitting lost password form.
     */
    @RequestMapping(path = "/login/lostPassword", method = RequestMethod.POST)
    public String lostPasswordFormSubmit(
        @Valid final LostPasswordForm lostPasswordForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        final Model model
        ) {
        // Redirect to home if already logged in
        if (isLoggedIn()) {
            return "redirect:/";
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            logger.info("Result: {}", lostPasswordForm);
            model.addAttribute("FlashMessage", FlashMessage.newWarning("Please submit required fields."));
            return "login/lostPasswordForm";
        }

        final FlashMessage flashMessage = FlashMessage.newInfo("Please check your E-mail address for a Password Reset Link.");
        redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

        // Retrieve User by Email
        final User user = userRepository.findByEmail(lostPasswordForm.getEmail());
        if (user != null) {
            // Do email reset request.
            //resetUserPasswordManager.requestPasswordReset(user);
        }

        // return success
        return "redirect:/login";
    }

    /**
     * GET Displays the Reset Password Form.
     */
    @RequestMapping(path = "/login/resetPassword", method = RequestMethod.GET)
    public String resetPasswordForm(final ResetPasswordForm resetPasswordForm) {
        // Redirect to home if already logged in
        if (isLoggedIn()) {
            return "redirect:/";
        }

        return "login/resetPasswordForm";
    }

    /**
     * POST Requests handle submitting reset password form.
     */
    @RequestMapping(path = "/login/resetPassword", method = RequestMethod.POST)
    public String resetPasswordFormSubmit(
        @Valid final ResetPasswordForm resetPasswordForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        final Model model
    ) {
        // Redirect to home if already logged in
        if (isLoggedIn()) {
            return "redirect:/";
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("FlashMessage", FlashMessage.newWarning("Please submit required fields."));
            return "login/resetPasswordForm";
        }

        // Retrieve User by Email
        final User user = userRepository.findByEmail(resetPasswordForm.getEmail());
        boolean result = false;
        if (user != null) {
            // Attempt reset
            //result = resetUserPasswordManager.resetPassword(user, resetPasswordForm.getToken(), resetPasswordForm.getPassword());
        }

        final FlashMessage flashMessage;
        if (result) {
            flashMessage = FlashMessage.newInfo("Successfully reset password, please login.");
        } else {
            flashMessage = FlashMessage.newWarning("Failed to reset password.");
        }
        redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

        // redirect to login
        return "redirect:/login";
    }

    /**
     * POST Requests handle submitting new user registration form.
     */
    @RequestMapping(path = "/me", method = RequestMethod.GET)
    public String me(Authentication auth) {
        if (isLoggedIn()) {
            return "redirect:/";
        }
        final CustomUserDetails userDetails = getLoggedInUser();
        final User user = userDetails.getUserModel();

        logger.info("User: {}", user);
        logger.info("User Role: {}", user.getRole().name());
        logger.info("Authorities: {}", auth.getAuthorities());
        return "redirect:/";
    }
}
