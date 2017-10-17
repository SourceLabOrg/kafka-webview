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
    @RequestMapping(path = "/{clusterId}", method = RequestMethod.GET)
    public String readCluster(
        final @PathVariable Long clusterId,
        final Model model,
        final RedirectAttributes redirectAttributes) {

        // Retrieve by id
        final Cluster cluster = retrieveCluster(clusterId, redirectAttributes);
        if (cluster == null) {
            // redirect
            return "redirect:/";
        }
        model.addAttribute("cluster", cluster);

        // Setup breadcrumbs
        setupBreadCrumbs(model)
            .addCrumb(cluster.getName(), null);

        // Display template
        return "cluster/read";
    }

    /**
     * GET Displays info about a specific broker in a cluster.
     */
    @RequestMapping(path = "/{clusterId}/broker/{brokerId}", method = RequestMethod.GET)
    public String readBroker(
        final @PathVariable Long clusterId,
        final @PathVariable Integer brokerId,
        final Model model,
        final RedirectAttributes redirectAttributes) {

        // Retrieve by id
        final Cluster cluster = retrieveCluster(clusterId, redirectAttributes);
        if (cluster == null) {
            // redirect
            return "redirect:/";
        }
        model.addAttribute("cluster", cluster);
        model.addAttribute("brokerId", brokerId);

        // Setup breadcrumbs
        setupBreadCrumbs(model)
            .addCrumb(cluster.getName(), "/cluster/" + clusterId)
            .addCrumb("Broker " + brokerId, null);


        // Display template
        return "cluster/readBroker";
    }

    /**
     * GET Displays info about a specific topic in a cluster.
     */
    @RequestMapping(path = "/{clusterId}/topic/{topic:.+}", method = RequestMethod.GET)
    public String readTopic(
        final @PathVariable Long clusterId,
        final @PathVariable String topic,
        final Model model,
        final RedirectAttributes redirectAttributes) {

        // Retrieve by id
        final Cluster cluster = retrieveCluster(clusterId, redirectAttributes);
        if (cluster == null) {
            // redirect
            return "redirect:/";
        }
        model.addAttribute("cluster", cluster);
        model.addAttribute("topic", topic);

        // Setup breadcrumbs
        setupBreadCrumbs(model)
            .addCrumb(cluster.getName(), "/cluster/" + clusterId)
            .addCrumb("Topic " + topic, null);


        // Display template
        return "cluster/readTopic";
    }

    private Cluster retrieveCluster(final Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve by id
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            // redirect
            // Set flash message
            final FlashMessage flashMessage = FlashMessage.newWarning("Unable to find cluster!");
            redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

            // redirect to cluster index
            return null;
        }
        return cluster;
    }

    private BreadCrumbManager setupBreadCrumbs(final Model model) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model);
        manager.addCrumb("Cluster Explorer", "/cluster");
        return manager;
    }
}
