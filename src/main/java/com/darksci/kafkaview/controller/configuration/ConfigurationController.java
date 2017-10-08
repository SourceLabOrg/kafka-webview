package com.darksci.kafkaview.controller.configuration;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.controller.configuration.forms.ClusterForm;
import com.darksci.kafkaview.controller.login.forms.LoginForm;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/configuration")
public class ConfigurationController extends BaseController {
    private final static Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "/cluster", method = RequestMethod.GET)
    public String index() {
        return "configuration/cluster/index";
    }

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "/cluster/create", method = RequestMethod.GET)
    public String createClusterForm(final ClusterForm clusterForm) {
        return "configuration/cluster/create";
    }

    @RequestMapping(path = "/cluster/create", method = RequestMethod.POST)
    public String createClusterSubmit(
        @Valid final ClusterForm clusterForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes) {
        // If we have errors
        if (bindingResult.hasErrors()) {
            logger.info("Result: {}", clusterForm);
            return "configuration/cluster/create";
        }

        // Create cluster
//        final User user = userRepository.findByEmail(loginForm.getEmail());
//        logger.info("User: {}", user);
//        if (user == null) {
//            bindingResult.addError(new FieldError("loginForm", "email", "Invalid Email or Password"));
//            return "login/loginForm";
//        }

        final FlashMessage flashMessage = FlashMessage.newSuccess("Created new cluster!");
        redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

        // Otherwise success
        // return success
        return "redirect:/configuration/cluster";
    }


}
