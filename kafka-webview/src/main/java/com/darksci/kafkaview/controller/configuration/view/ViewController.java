package com.darksci.kafkaview.controller.configuration.view;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.controller.configuration.view.forms.ViewForm;
import com.darksci.kafkaview.manager.kafka.KafkaAdminFactory;
import com.darksci.kafkaview.manager.kafka.KafkaOperations;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.dto.TopicDetails;
import com.darksci.kafkaview.manager.kafka.dto.TopicList;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.model.MessageFormat;
import com.darksci.kafkaview.model.View;
import com.darksci.kafkaview.repository.ClusterRepository;
import com.darksci.kafkaview.repository.MessageFormatRepository;
import com.darksci.kafkaview.repository.ViewRepository;
import org.apache.kafka.clients.admin.AdminClient;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/configuration/view")
public class ViewController extends BaseController {

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private MessageFormatRepository messageFormatRepository;

    @Autowired
    private ViewRepository viewRepository;

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Retrieve all message formats
        final Iterable<View> viewList = viewRepository.findAll();
        model.addAttribute("views", viewList);

        return "configuration/view/index";
    }

    /**
     * GET Displays create view form
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createViewForm(final ViewForm viewForm, final Model model) {

        // Retrieve all clusters
        model.addAttribute("clusters", clusterRepository.findAllByOrderByNameAsc());

        // Retrieve all message formats
        model.addAttribute("defaultMessageFormats", messageFormatRepository.findByIsDefaultFormatOrderByNameAsc(true));
        model.addAttribute("customMessageFormats", messageFormatRepository.findByIsDefaultFormatOrderByNameAsc(false));

        // If we have a cluster Id
        model.addAttribute("topics", new ArrayList<>());
        model.addAttribute("partitions", new ArrayList<>());
        if (viewForm.getClusterId() != null) {
            // Lets load the topics now
            // Retrieve cluster
            final Cluster cluster = clusterRepository.findOne(viewForm.getClusterId());
            if (cluster != null) {
                // Create a new Operational Client
                final ClusterConfig clusterConfig = new ClusterConfig(cluster.getBrokerHosts());
                final AdminClient adminClient = new KafkaAdminFactory(clusterConfig, "BobsYerAunty").create();

                try (final KafkaOperations operations = new KafkaOperations(adminClient)) {
                    final TopicList topics = operations.getAvailableTopics();
                    model.addAttribute("topics", topics.getTopics());

                    // If we have a selected topic
                    if (viewForm.getTopic() != null && !"!".equals(viewForm.getTopic())) {
                        final TopicDetails topicDetails = operations.getTopicDetails(viewForm.getTopic());
                        model.addAttribute("partitions", topicDetails.getPartitions());
                    }
                }
            }
        }

        return "configuration/view/create";
    }

    /**
     * GET Displays edit view form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editViewForm(
        final @PathVariable Long id,
        final ViewForm viewForm,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Retrieve by id
        final View view = viewRepository.findOne(id);
        if (view == null) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));

            // redirect to view index
            return "redirect:/configuration/view";
        }

        // Build form
        viewForm.setId(view.getId());
        viewForm.setName(view.getName());
        viewForm.setClusterId(view.getCluster().getId());
        viewForm.setKeyMessageFormatId(view.getKeyMessageFormat().getId());
        viewForm.setValueMessageFormatId(view.getValueMessageFormat().getId());
        viewForm.setTopic(view.getTopic());
        viewForm.setPartitions(view.getPartitionsAsSet());
        viewForm.setResultsPerPartition(view.getResultsPerPartition());

        return createViewForm(viewForm, model);
    }

    /**
     * Handles both Update and Creating views.
     */
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public String updateView(
        @Valid final ViewForm viewForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Determine if we're updating or creating
        final boolean updateExisting = viewForm.exists();

        // Ensure that cluster name is not alreadyx used.
        final View existingView = viewRepository.findByName(viewForm.getName());
        if (existingView != null) {
            // If we're updating, exclude our own id.
            if (!updateExisting ||
                (updateExisting && !viewForm.getId().equals(existingView.getId()))) {
                bindingResult.addError(new FieldError(
                    "viewForm", "name", viewForm.getName(), true, null, null, "Name is already used")
                );
            }
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            return createViewForm(viewForm, model);
        }

        // If we're updating
        final View view;
        final String successMessage;
        if (updateExisting) {
            // Retrieve it
            view = viewRepository.findOne(viewForm.getId());
            if (view == null) {
                // Set flash message and redirect
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));

                // redirect to view index
                return "redirect:/configuration/view";
            }

            successMessage = "Updated view successfully!";
        } else {
            view = new View();
            successMessage = "Created new view!";
        }

        // Update properties
        final MessageFormat keyMessageFormat = messageFormatRepository.findOne(viewForm.getKeyMessageFormatId());
        final MessageFormat valueMessageFormat = messageFormatRepository.findOne(viewForm.getValueMessageFormatId());
        final Cluster cluster = clusterRepository.findOne(viewForm.getClusterId());

        final Set<Integer> partitionIds = viewForm.getPartitions();
        final String partitionsStr = partitionIds.stream().map(Object::toString).collect(Collectors.joining(","));

        view.setName(viewForm.getName());
        view.setTopic(viewForm.getTopic());
        view.setKeyMessageFormat(keyMessageFormat);
        view.setValueMessageFormat(valueMessageFormat);
        view.setCluster(cluster);
        view.setPartitions(partitionsStr);
        view.setResultsPerPartition(viewForm.getResultsPerPartition());
        viewRepository.save(view);

        // Set flash message
        redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess(successMessage));

        // redirect to cluster index
        return "redirect:/configuration/view";
    }

    /**
     * POST deletes the selected view
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String deleteView(final @PathVariable Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        final View view = viewRepository.findOne(id);
        if (view == null) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));
        } else {
            // Delete it
            viewRepository.delete(id);

            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Deleted view!"));
        }

        // redirect to cluster index
        return "redirect:/configuration/view";
    }
}
