package com.darksci.kafkaview.controller.browser;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.manager.ui.BreadCrumbManager;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.View;
import com.darksci.kafkaview.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/browser")
public class BrowserController extends BaseController {
    @Autowired
    private ViewRepository viewRepository;

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public String index(
        final @PathVariable Long id,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Retrieve the browser
        final View view = viewRepository.findOne(id);
        if (view == null) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find browser!"));

            // redirect to home
            return "redirect:/";
        }

        // Setup breadcrumbs
        new BreadCrumbManager(model)
            .addCrumb("Browser", "/browser")
            .addCrumb(view.getName(), "/browser/" + view.getId());

        // Set model Attributes
        model.addAttribute("view", view);
        model.addAttribute("cluster", view.getCluster());

        return "browser/index";
    }
}
