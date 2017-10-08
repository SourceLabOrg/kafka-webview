package com.darksci.kafkaview.controller.configuration;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.controller.configuration.forms.ClusterForm;
import com.darksci.kafkaview.controller.login.forms.LoginForm;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.model.User;
import com.darksci.kafkaview.repository.ClusterRepository;
import com.darksci.kafkaview.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

@Controller
@RequestMapping("/configuration")
public class ConfigurationController extends BaseController {
    private final static Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    @Autowired
    private ClusterRepository clusterRepository;

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "/cluster", method = RequestMethod.GET)
    public String index(final Model model) {
        // Retrieve all clusters
        final Iterable<Cluster> clusterList = clusterRepository.findAll();
        model.addAttribute("clusterList", clusterList);

        return "configuration/cluster/index";
    }

    /**
     * GET Displays create cluster form.
     */
    @RequestMapping(path = "/cluster/create", method = RequestMethod.GET)
    public String createClusterForm(final ClusterForm clusterForm) {
        return "configuration/cluster/create";
    }

    /**
     * GET Displays edit cluster form.
     */
    @RequestMapping(path = "/cluster/edit/{id}", method = RequestMethod.GET)
    public String editClusterForm(
        final @PathVariable Long id,
        final ClusterForm clusterForm,
        final RedirectAttributes redirectAttributes) {

        // Retrieve by id
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            // redirect
            // Set flash message
            final FlashMessage flashMessage = FlashMessage.newWarning("Unable to find cluster!");
            redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

            // redirect to cluster index
            return "redirect:/configuration/cluster";
        }

        // Build form
        clusterForm.setId(cluster.getId());
        clusterForm.setName(cluster.getName());
        clusterForm.setBrokerHosts(cluster.getBrokerHosts());

        // Display template
        return "configuration/cluster/create";
    }

    @RequestMapping(path = "/cluster/create", method = RequestMethod.POST)
    public String createClusterSubmit(
        @Valid final ClusterForm clusterForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes) {

        // Ensure that cluster name is not already used.
        final Cluster existingCluster = clusterRepository.findByName(clusterForm.getName());
        if (existingCluster != null) {
            bindingResult.addError(new FieldError("clusterForm", "name", "Name is already used"));
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            logger.info("Result: {}", clusterForm);
            return "configuration/cluster/create";
        }

        // Create cluster
        final Cluster cluster = new Cluster();
        cluster.setName(clusterForm.getName());
        cluster.setBrokerHosts(clusterForm.getBrokerHosts());
        clusterRepository.save(cluster);

        // Set flash message
        final FlashMessage flashMessage = FlashMessage.newSuccess("Created new cluster!");
        redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

        // redirect to cluster index
        return "redirect:/configuration/cluster";
    }
}
