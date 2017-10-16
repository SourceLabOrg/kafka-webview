package com.darksci.kafkaview.controller.configuration.user;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.manager.ui.BreadCrumbManager;
import com.darksci.kafkaview.model.User;
import com.darksci.kafkaview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/configuration/user")
public class UserController extends BaseController {

    @Autowired
    private UserRepository userRepository;

    /**
     * GET Displays main user index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all message formats
        final Iterable<User> usersList = userRepository.findAll();
        model.addAttribute("users", usersList);

        return "configuration/user/index";
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
