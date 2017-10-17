package com.darksci.kafkaview.controller.cluster;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.manager.ui.BreadCrumbManager;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.repository.ClusterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cluster")
public class ClusterController extends BaseController {

    @Autowired
    private ClusterRepository clusterRepository;

    /**
     * GET Displays edit cluster form.
     */
    @RequestMapping(path = "/read/{id}", method = RequestMethod.GET)
    public String readCluster(final @PathVariable Long id, final Model model, final RedirectAttributes redirectAttributes) {

        // Retrieve by id
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            // redirect
            // Set flash message
            final FlashMessage flashMessage = FlashMessage.newWarning("Unable to find cluster!");
            redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

            // redirect to cluster index
            return "redirect:/";
        }
        model.addAttribute("cluster", cluster);

        // Setup breadcrumbs
        setupBreadCrumbs(model,  cluster.getName(), null);

        // Display template
        return "cluster/read";
    }

    private void setupBreadCrumbs(final Model model, String name, String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model);

        if (name != null) {
            manager.addCrumb("Cluster Explorer", "/cluster");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Cluster Explorer", null);
        }
    }
}
