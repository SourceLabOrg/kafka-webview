package com.darksci.kafkaview.controller.configuration.cluster;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.controller.configuration.cluster.forms.ClusterForm;
import com.darksci.kafkaview.manager.kafka.KafkaConsumerFactory;
import com.darksci.kafkaview.manager.kafka.KafkaOperations;
import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.config.DeserializerConfig;
import com.darksci.kafkaview.manager.kafka.config.FilterConfig;
import com.darksci.kafkaview.manager.kafka.config.TopicConfig;
import com.darksci.kafkaview.manager.kafka.dto.TopicList;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.repository.ClusterRepository;
import org.apache.kafka.common.serialization.StringDeserializer;
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

@Controller
@RequestMapping("/configuration/cluster")
public class ClusterController extends BaseController {
    private final static Logger logger = LoggerFactory.getLogger(ClusterController.class);

    @Autowired
    private ClusterRepository clusterRepository;

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Retrieve all clusters
        final Iterable<Cluster> clusterList = clusterRepository.findAll();
        model.addAttribute("clusterList", clusterList);

        return "configuration/cluster/index";
    }

    /**
     * GET Displays create cluster form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createClusterForm(final ClusterForm clusterForm) {
        return "configuration/cluster/create";
    }

    /**
     * GET Displays edit cluster form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
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

    /**
     * Handles both Update and Creating clusters.
     */
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public String clusterUpdate(
        @Valid final ClusterForm clusterForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes) {

        final boolean updateExisting = clusterForm.exists();

        // Ensure that cluster name is not already used.
        final Cluster existingCluster = clusterRepository.findByName(clusterForm.getName());
        if (existingCluster != null) {
            // If we're updating, exclude our own id.
            if (!updateExisting ||
                (updateExisting && !clusterForm.getId().equals(existingCluster.getId()))) {
                bindingResult.addError(new FieldError(
                    "clusterForm", "name", clusterForm.getName(), true, null, null, "Name is already used")
                );
            }
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            logger.info("Result: {}", clusterForm);
            return "configuration/cluster/create";
        }

        // If we're updating
        final Cluster cluster;
        final String successMessage;
        if (updateExisting) {
            // Retrieve it
            cluster = clusterRepository.findOne(clusterForm.getId());
            if (cluster == null) {
                // redirect
                // Set flash message
                final FlashMessage flashMessage = FlashMessage.newWarning("Unable to find cluster!");
                redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

                // redirect to cluster index
                return "redirect:/configuration/cluster";
            }

            successMessage = "Updated cluster successfully!";
        } else {
            cluster = new Cluster();
            successMessage = "Created new cluster!";
        }

        // Update properties
        cluster.setName(clusterForm.getName());
        cluster.setBrokerHosts(clusterForm.getBrokerHosts());
        cluster.setValid(false);
        clusterRepository.save(cluster);

        // Set flash message
        final FlashMessage flashMessage = FlashMessage.newSuccess(successMessage);
        redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

        // redirect to cluster index
        return "redirect:/configuration/cluster";
    }

    /**
     * POST deletes the selected cluster
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String deleteCluster(final @PathVariable Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find cluster!"));
        } else {
            // Delete it
            clusterRepository.delete(id);

            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Deleted cluster!"));
        }

        // redirect to cluster index
        return "redirect:/configuration/cluster";
    }
}