/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.controller.configuration.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.TimeoutException;
import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.configuration.view.forms.ViewForm;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaOperations;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaOperationsFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicList;
import org.sourcelab.kafka.webview.ui.manager.model.view.ViewCopyManager;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.manager.ui.datatable.ActionTemplate;
import org.sourcelab.kafka.webview.ui.manager.ui.datatable.Datatable;
import org.sourcelab.kafka.webview.ui.manager.ui.datatable.DatatableColumn;
import org.sourcelab.kafka.webview.ui.manager.ui.datatable.DatatableFilter;
import org.sourcelab.kafka.webview.ui.manager.ui.datatable.LinkTemplate;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterEnforced;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterOptional;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.repository.FilterRepository;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewToFilterOptionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private ViewToFilterOptionalRepository viewToFilterOptionalRepository;

    @Autowired
    private FilterRepository filterRepository;

    @Autowired
    private KafkaOperationsFactory kafkaOperationsFactory;


    /**
     * GET views index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String datatable(
        final Model model,
        @RequestParam(name = "cluster.id", required = false) final Long clusterId,
        final Pageable pageable,
        @RequestParam Map<String,String> allParams
    ) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Determine if we actually have any clusters setup
        // Retrieve all clusters and index by id
        final Map<Long, Cluster> clustersById = new HashMap<>();
        clusterRepository
            .findAllByOrderByNameAsc()
            .forEach((cluster) -> clustersById.put(cluster.getId(), cluster));

        // Create a filter
        final List<DatatableFilter.FilterOption> filterOptions = new ArrayList<>();
        clustersById
            .forEach((id, cluster) -> filterOptions.add(new DatatableFilter.FilterOption(String.valueOf(id), cluster.getName())));
        final DatatableFilter filter = new DatatableFilter("Cluster", "clusterId", filterOptions);
        model.addAttribute("filters", new DatatableFilter[] { filter });

        final Datatable.Builder<View> builder = Datatable.newBuilder(View.class)
            .withRepository(viewRepository)
            .withPageable(pageable)
            .withRequestParams(allParams)
            .withUrl("/configuration/view")
            .withLabel("Views")
            .withCreateLink("/configuration/view/create")

            // Name Column
            .withColumn(DatatableColumn.newBuilder(View.class)
                .withFieldName("name")
                .withLabel("Name")
                .withIsSortable(true)
                .withRenderTemplate(new LinkTemplate<>(
                    (record) -> "/view/" + record.getId(),
                    View::getName
                )).build())

            // Cluster Column
            .withColumn(DatatableColumn.newBuilder(View.class)
                .withFieldName("cluster.name")
                .withLabel("Cluster")
                .withRenderTemplate(new LinkTemplate<>(
                    (record) -> "/cluster/" + record.getCluster().getId(),
                    (record) -> record.getCluster().getName()
                ))
                .withIsSortable(true)
                .build())

            // Topic Column
            .withColumn(DatatableColumn.newBuilder(View.class)
                .withFieldName("topic")
                .withLabel("Topic")
                .withRenderFunction(View::getTopic)
                .withIsSortable(true)
                .build())

            // Partitions Column
            .withColumn(DatatableColumn.newBuilder(View.class)
                .withFieldName("partitions")
                .withLabel("Partitions")
                .withRenderFunction(View::displayPartitions)
                .withIsSortable(false)
                .build())

            // Key Format Column
            .withColumn(DatatableColumn.newBuilder(View.class)
                .withFieldName("keyMessageFormat.name")
                .withLabel("Key Format")
                .withRenderFunction((view) -> view.getKeyMessageFormat().getName())
                .withIsSortable(true)
                .build())

            // Value Format Column
            .withColumn(DatatableColumn.newBuilder(View.class)
                .withFieldName("valueMessageFormat.name")
                .withLabel("Value Format")
                .withRenderFunction((view) -> view.getValueMessageFormat().getName())
                .withIsSortable(true)
                .build())

            // Action Column
            .withColumn(DatatableColumn.newBuilder(View.class)
                .withLabel("Action")
                .withFieldName("id")
                .withIsSortable(false)
                .withHeaderAlignRight()
                .withRenderTemplate(ActionTemplate.newBuilder(View.class)
                    // Edit Link
                    .withEditLink(View.class, (record) -> "/configuration/view/edit/" + record.getId())
                    // Copy Link
                    .withCopyLink(View.class, (record) -> "/configuration/view/copy/" + record.getId())
                    // Delete Link
                    .withDeleteLink(View.class, (record) -> "/configuration/view/delete/" + record.getId())
                    .build())
                .build())

            // Filters
            .withFilter(new DatatableFilter("Cluster", "cluster.id", filterOptions))
            .withSearch("name");

        // Add datatable attribute
        model.addAttribute("datatable", builder.build());

        // Determine if we have no clusters setup so we can show appropriate inline help.
        model.addAttribute("hasClusters", !clustersById.isEmpty());
        model.addAttribute("hasNoClusters", clustersById.isEmpty());
        model.addAttribute("hasViews", viewRepository.count() > 0);

        return "configuration/view/index";
    }

    /**
     * GET Displays create view form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createViewForm(final ViewForm viewForm, final Model model, final RedirectAttributes redirectAttributes) {
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
        model.addAttribute("filterParameters", new HashMap<Long, Map<String, String>>());

        if (viewForm.getClusterId() != null) {
            // Lets load the topics now
            // Retrieve cluster
            clusterRepository.findById(viewForm.getClusterId()).ifPresent((cluster) -> {
                try (final KafkaOperations operations = kafkaOperationsFactory.create(cluster, getLoggedInUserId())) {
                    final TopicList topics = operations.getAvailableTopics();
                    model.addAttribute("topics", topics.getTopics());

                    // If we have a selected topic
                    if (viewForm.getTopic() != null && !"!".equals(viewForm.getTopic())) {
                        final TopicDetails topicDetails = operations.getTopicDetails(viewForm.getTopic());
                        model.addAttribute("partitions", topicDetails.getPartitions());
                    }
                    redirectAttributes.getFlashAttributes().remove("ClusterError");
                } catch (final TimeoutException timeoutException) {
                    final String reason = timeoutException.getMessage() + " (This may indicate an authentication or connection problem)";
                    // Set error msg
                    redirectAttributes.addFlashAttribute("ClusterError", true);
                    redirectAttributes.addFlashAttribute(
                        "FlashMessage",
                        FlashMessage.newDanger(
                            "Error connecting to cluster '" + cluster.getName() + "': " + reason,
                            timeoutException
                        )
                    );
                }
            });
        }

        // If we had an error.
        if (redirectAttributes.getFlashAttributes().containsKey("ClusterError")) {
            // Redirect instead.
            return "redirect:/configuration/view";
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
        final Optional<View> viewOptional = viewRepository.findById(id);
        if (!viewOptional.isPresent()) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));

            // redirect to view index
            return "redirect:/configuration/view";
        }
        final View view = viewOptional.get();

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

        final ObjectMapper objectMapper = new ObjectMapper();

        // Set enforced filters
        final Map<Long, Map<String, String>> filterParameters = new HashMap<>();
        final Set<Long> enforcedFilterIds = new HashSet<>();
        for (final ViewToFilterEnforced enforcedFilter: view.getEnforcedFilters()) {
            enforcedFilterIds.add(enforcedFilter.getFilter().getId());

            // Get options
            try {
                final Map<String, String> optionParameters = objectMapper.readValue(enforcedFilter.getOptionParameters(), Map.class);
                filterParameters.put(enforcedFilter.getFilter().getId(), optionParameters);
            } catch (IOException e) {
                // Failed to parse?  Wipe out value
                enforcedFilter.setOptionParameters("{}");
            }
        }
        viewForm.setEnforcedFilters(enforcedFilterIds);
        model.addAttribute("filterParameters", filterParameters);

        // Set optional filters
        final Set<Long> optionalFilterIds = new HashSet<>();
        for (final ViewToFilterOptional optionalFilter: view.getOptionalFilters()) {
            optionalFilterIds.add(optionalFilter.getFilter().getId());
        }
        viewForm.setOptionalFilters(optionalFilterIds);

        return createViewForm(viewForm, model, redirectAttributes);
    }

    /**
     * Handles both Update and Creating views.
     */
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public String updateView(
        @Valid final ViewForm viewForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        final Model model,
        @RequestParam final Map<String, String> allRequestParams) {

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
            return createViewForm(viewForm, model, redirectAttributes);
        }

        // If we're updating
        final View view;
        final String successMessage;
        if (updateExisting) {
            // Retrieve it
            final Optional<View> viewOptional = viewRepository.findById(viewForm.getId());
            if (!viewOptional.isPresent()) {
                // Set flash message and redirect
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));

                // redirect to view index
                return "redirect:/configuration/view";
            }
            view = viewOptional.get();

            successMessage = "Updated view successfully!";
        } else {
            view = new View();
            view.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            successMessage = "Created new view!";
        }

        // Update properties
        final MessageFormat keyMessageFormat = messageFormatRepository.findById(viewForm.getKeyMessageFormatId()).get();
        final MessageFormat valueMessageFormat = messageFormatRepository.findById(viewForm.getValueMessageFormatId()).get();
        final Cluster cluster = clusterRepository.findById(viewForm.getClusterId()).get();

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
        handleEnforcedFilterSubmission(
            view.getEnforcedFilters(),
            viewForm.getEnforcedFilters(),
            view,
            allRequestParams);

        // Handle optional filters
        handleOptionalFilterSubmission(view.getOptionalFilters(), viewForm.getOptionalFilters(), view);

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
    private void handleOptionalFilterSubmission(
        final Set<ViewToFilterOptional> currentlySetFilters,
        final Set<Long> submittedFilterIds,
        final View view) {

        final Set<Long> enabledFilterIds = new HashSet<>();
        long sortOrder = 0;
        for (final Long filterId : submittedFilterIds) {
            // Skip invalids
            if (filterId == null || filterId.equals(0)) {
                continue;
            }

            // Retrieve filter
            final Optional<Filter> filterOptional = filterRepository.findById(filterId);
            if (!filterOptional.isPresent()) {
                continue;
            }
            final Filter filter = filterOptional.get();

            ViewToFilterOptional viewToFilterOptional = null;
            for (final ViewToFilterOptional currentEntry: currentlySetFilters) {
                if (currentEntry.getFilter().getId() == filterId) {
                    // Update existing
                    viewToFilterOptional = currentEntry;
                    break;
                }
            }
            if (viewToFilterOptional == null) {
                // Create new entry
                viewToFilterOptional = new ViewToFilterOptional();
            }

            viewToFilterOptional.setFilter(filter);
            viewToFilterOptional.setView(view);
            viewToFilterOptional.setSortOrder(sortOrder++);

            currentlySetFilters.add(viewToFilterOptional);
            enabledFilterIds.add(filterId);
        }

        final Set<ViewToFilterOptional> toRemoveFilters = new HashSet<>();
        for (final ViewToFilterOptional optionalFilter: currentlySetFilters) {
            if (!enabledFilterIds.contains(optionalFilter.getFilter().getId())) {
                toRemoveFilters.add(optionalFilter);
            }
        }
        if (!toRemoveFilters.isEmpty()) {
            currentlySetFilters.removeAll(toRemoveFilters);
        }
    }

    /**
     * Handle adding/removing filters submitted.
     * @param currentlySetFilters Set of Filters currently set on the view.
     * @param submittedFilterIds Set of FilterIds submitted with the form.
     */
    private void handleEnforcedFilterSubmission(
        final Set<ViewToFilterEnforced> currentlySetFilters,
        final Set<Long> submittedFilterIds,
        final View view,
        final Map<String, String> allRequestParameters) {

        // For converting map to json string
        final ObjectMapper objectMapper = new ObjectMapper();

        // Loop over filters submitted
        final Set<Long> enabledFilterIds = new HashSet<>();
        long sortOrder = 0;
        for (final Long filterId : submittedFilterIds) {
            // Skip invalids
            if (filterId == null || filterId.equals(0)) {
                continue;
            }

            // Retrieve filter
            final Optional<Filter> filterOptional = filterRepository.findById(filterId);
            if (!filterOptional.isPresent()) {
                continue;
            }
            final Filter filter = filterOptional.get();

            ViewToFilterEnforced viewToFilterEnforced = null;
            for (final ViewToFilterEnforced currentEntry: currentlySetFilters) {
                if (currentEntry.getFilter().getId() == filterId) {
                    viewToFilterEnforced = currentEntry;
                    break;
                }
            }
            if (viewToFilterEnforced == null) {
                // Create new entry
                viewToFilterEnforced = new ViewToFilterEnforced();
            }

            // Grab options
            final Set<String> optionNames = filter.getOptionsAsSet();
            final Map<String, String> optionValues = new HashMap<>();
            for (final String optionName: optionNames) {
                // Generate the name of the request parameter
                final String requestParamName = filter.getId() + "-" + optionName;
                if (allRequestParameters.containsKey(requestParamName)) {
                    optionValues.put(optionName, allRequestParameters.get(requestParamName));
                } else {
                    optionValues.put(optionName, "");
                }
            }

            // Update properties
            viewToFilterEnforced.setFilter(filter);
            viewToFilterEnforced.setView(view);
            viewToFilterEnforced.setSortOrder(sortOrder++);

            // Convert to json and store on the relationship
            final String jsonStr;
            try {
                jsonStr = objectMapper.writeValueAsString(optionValues);
                viewToFilterEnforced.setOptionParameters(jsonStr);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            currentlySetFilters.add(viewToFilterEnforced);
            enabledFilterIds.add(filterId);
        }

        final Set<ViewToFilterEnforced> toRemoveFilters = new HashSet<>();
        for (final ViewToFilterEnforced enforcedFilter: currentlySetFilters) {
            if (!enabledFilterIds.contains(enforcedFilter.getFilter().getId())) {
                toRemoveFilters.add(enforcedFilter);
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
        if (!viewRepository.existsById(id)) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));
        } else {
            // Delete it
            viewRepository.deleteById(id);

            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Deleted view!"));
        }

        // redirect to cluster index
        return "redirect:/configuration/view";
    }

    /**
     * POST copies the selected view.
     */
    @RequestMapping(path = "/copy/{id}", method = RequestMethod.POST)
    @Transactional
    public String copyView(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        if (!viewRepository.existsById(id)) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));
        } else {
            // Retrieve view
            viewRepository.findById(id).ifPresent((view) -> {
                // Create Copy manager
                final ViewCopyManager copyManager = new ViewCopyManager(viewRepository);
                final String newName = "Copy of " + view.getName();
                copyManager.copy(view, newName);

                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newSuccess("Copied view using name '" + newName + "'!")
                );
            });
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
