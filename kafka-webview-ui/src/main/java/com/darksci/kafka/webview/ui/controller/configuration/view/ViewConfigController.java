package com.darksci.kafka.webview.ui.controller.configuration.view;

import com.darksci.kafka.webview.ui.controller.BaseController;
import com.darksci.kafka.webview.ui.controller.configuration.view.forms.ViewForm;
import com.darksci.kafka.webview.ui.manager.kafka.KafkaOperations;
import com.darksci.kafka.webview.ui.manager.kafka.KafkaOperationsFactory;
import com.darksci.kafka.webview.ui.manager.kafka.dto.TopicDetails;
import com.darksci.kafka.webview.ui.manager.kafka.dto.TopicList;
import com.darksci.kafka.webview.ui.manager.ui.BreadCrumbManager;
import com.darksci.kafka.webview.ui.manager.ui.FlashMessage;
import com.darksci.kafka.webview.ui.model.Cluster;
import com.darksci.kafka.webview.ui.model.Filter;
import com.darksci.kafka.webview.ui.model.MessageFormat;
import com.darksci.kafka.webview.ui.model.View;
import com.darksci.kafka.webview.ui.repository.ClusterRepository;
import com.darksci.kafka.webview.ui.repository.FilterRepository;
import com.darksci.kafka.webview.ui.repository.MessageFormatRepository;
import com.darksci.kafka.webview.ui.repository.ViewRepository;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for CRUD over View entities.
 */
@Controller
@RequestMapping("/configuration/view")
public class ViewConfigController extends BaseController {

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private MessageFormatRepository messageFormatRepository;

    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private FilterRepository filterRepository;

    @Autowired
    private KafkaOperationsFactory kafkaOperationsFactory;

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all message formats
        final Iterable<View> viewList = viewRepository.findAll();
        model.addAttribute("views", viewList);

        return "configuration/view/index";
    }

    /**
     * GET Displays create view form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createViewForm(final ViewForm viewForm, final Model model) {
        // Setup breadcrumbs
        if (!model.containsAttribute("BreadCrumbs")) {
            setupBreadCrumbs(model, "Create", null);
        }

        // Retrieve all clusters
        model.addAttribute("clusters", clusterRepository.findAllByOrderByNameAsc());

        // Retrieve all message formats
        model.addAttribute("defaultMessageFormats", messageFormatRepository.findByIsDefaultFormatOrderByNameAsc(true));
        model.addAttribute("customMessageFormats", messageFormatRepository.findByIsDefaultFormatOrderByNameAsc(false));

        // If we have a cluster Id
        model.addAttribute("topics", new ArrayList<>());
        model.addAttribute("partitions", new ArrayList<>());

        // Retrieve all filters
        model.addAttribute("filterList", filterRepository.findAllByOrderByNameAsc());

        if (viewForm.getClusterId() != null) {
            // Lets load the topics now
            // Retrieve cluster
            final Cluster cluster = clusterRepository.findOne(viewForm.getClusterId());
            if (cluster != null) {
                try (final KafkaOperations operations = kafkaOperationsFactory.create(cluster, getLoggedInUserId())) {
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
        @PathVariable final Long id,
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

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Edit: " + view.getName(), null);

        // Build form
        viewForm.setId(view.getId());
        viewForm.setName(view.getName());
        viewForm.setClusterId(view.getCluster().getId());
        viewForm.setKeyMessageFormatId(view.getKeyMessageFormat().getId());
        viewForm.setValueMessageFormatId(view.getValueMessageFormat().getId());
        viewForm.setTopic(view.getTopic());
        viewForm.setPartitions(view.getPartitionsAsSet());
        viewForm.setResultsPerPartition(view.getResultsPerPartition());

        // Set enforced filters
        final Set<Long> enforcedFilterIds = new HashSet<>();
        for (final Filter filter: view.getEnforcedFilters()) {
            enforcedFilterIds.add(filter.getId());
        }
        viewForm.setEnforcedFilters(enforcedFilterIds);

        // Set optional filters
        final Set<Long> optionalFilterIds = new HashSet<>();
        for (final Filter filter: view.getOptionalFilters()) {
            optionalFilterIds.add(filter.getId());
        }
        viewForm.setOptionalFilters(optionalFilterIds);

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
            if (!updateExisting
                || (updateExisting && !viewForm.getId().equals(existingView.getId()))) {
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
            view.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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
        view.setResultsPerPartition(viewForm.getResultsPerPartition());
        view.setPartitions(partitionsStr);

        // Handle enforced filters
        handleFilterSubmission(view.getEnforcedFilters(), viewForm.getEnforcedFilters());

        // Handle optional filters
        handleFilterSubmission(view.getOptionalFilters(), viewForm.getOptionalFilters());

        // Persist the view
        view.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        viewRepository.save(view);

        // Set flash message
        redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess(successMessage));

        // redirect to cluster index
        return "redirect:/configuration/view";
    }

    /**
     * Handle adding/removing filters submitted.
     * @param currentlySetFilters Set of Filters currently set on the view.
     * @param submittedFilterIds Set of FilterIds submitted with the form.
     */
    private void handleFilterSubmission(final Set<Filter> currentlySetFilters, final Set<Long> submittedFilterIds) {
        // Loop over filters submitted
        final Set<Long> enabledFilterIds = new HashSet<>();
        for (final Long filterId : submittedFilterIds) {
            // Skip invalids
            if (filterId == null || filterId.equals(0)) {
                continue;
            }

            // Retrieve filter
            final Filter filter = filterRepository.findOne(filterId);
            if (filter == null) {
                continue;
            }
            currentlySetFilters.add(filter);
            enabledFilterIds.add(filterId);
        }

        final Set<Filter> toRemoveFilters = new HashSet<>();
        for (final Filter filter: currentlySetFilters) {
            if (!enabledFilterIds.contains(filter.getId())) {
                toRemoveFilters.add(filter);
            }
        }
        if (!toRemoveFilters.isEmpty()) {
            currentlySetFilters.removeAll(toRemoveFilters);
        }
    }

    /**
     * POST deletes the selected view.
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String deleteView(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
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

    private void setupBreadCrumbs(final Model model, String name, String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
            .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb("Views", "/configuration/view");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Views", null);
        }
    }
}
