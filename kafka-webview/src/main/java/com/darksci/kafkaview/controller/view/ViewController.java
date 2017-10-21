package com.darksci.kafkaview.controller.view;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.manager.ui.BreadCrumbManager;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.model.View;
import com.darksci.kafkaview.repository.ClusterRepository;
import com.darksci.kafkaview.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/view")
public class ViewController extends BaseController {
    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private ClusterRepository clusterRepository;

    /**
     * GET views index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {

        // Setup breadcrumbs
        new BreadCrumbManager(model)
            .addCrumb("View", null);

        // Retrieve all clusters
        final Iterable<Cluster> clusters = clusterRepository.findAllByOrderByNameAsc();
        final Map<Long, Iterable<View>> viewsByClusterId = new HashMap<>();

        for (final Cluster cluster: clusters) {
            // Retrieve all views for cluster
            final Iterable<View> views = viewRepository.findAllByClusterIdOrderByNameAsc(cluster.getId());
            viewsByClusterId.put(cluster.getId(), views);
        }

        // Set model Attributes
        model.addAttribute("viewsByClusterId", viewsByClusterId);
        model.addAttribute("clusters", clusters);

        return "view/index";
    }

    /**
     * GET Displays view for specified view.
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public String index(
        final @PathVariable Long id,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Retrieve the view
        final View view = viewRepository.findOne(id);
        if (view == null) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));

            // redirect to home
            return "redirect:/";
        }

        // Setup breadcrumbs
        new BreadCrumbManager(model)
            .addCrumb("View", "/view")
            .addCrumb(view.getName(), "/view/" + view.getId());

        // Set model Attributes
        model.addAttribute("view", view);
        model.addAttribute("cluster", view.getCluster());

        return "view/consume";
    }
}
