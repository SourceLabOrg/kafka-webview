package com.darksci.kafka.webview.ui.controller.home;

import com.darksci.kafka.webview.ui.controller.BaseController;
import com.darksci.kafka.webview.ui.manager.ui.BreadCrumbManager;
import com.darksci.kafka.webview.ui.repository.ClusterRepository;
import com.darksci.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController extends BaseController {

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private ViewRepository viewRepository;

    /**
     * Once things are setup, we'll redirect the home page to view/index.
     */
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(final Model model) {
        // Look for views
        final long numberOfViews = viewRepository.count();

        // If at least one view is configured
        if (numberOfViews > 0) {
            // Redirect to the vew index
            return "redirect:/view";
        }

        // Setup breadcrumbs
        new BreadCrumbManager(model);

        // Look for clusters
        final long numberOfClusters = clusterRepository.count();

        model.addAttribute("hasView", (numberOfViews > 0));
        model.addAttribute("hasCluster", (numberOfClusters > 0));
        return "home/index";
    }

    /**
     * Provides in App Help documentation.
     */
    @RequestMapping(path = "/help", method = RequestMethod.GET)
    public String help(final Model model) {
        // Setup breadcrumbs
        new BreadCrumbManager(model)
            .addCrumb("Help");

        return "home/help";
    }
}
