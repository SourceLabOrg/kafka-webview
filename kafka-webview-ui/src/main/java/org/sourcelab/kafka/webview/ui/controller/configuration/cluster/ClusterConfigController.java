/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.controller.configuration.cluster;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.configuration.cluster.forms.ClusterForm;
import org.sourcelab.kafka.webview.ui.manager.SensitiveConfigScrubber;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaClientConfigUtil;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaOperations;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaOperationsFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.UploadManager;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslProperties;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslUtility;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for Cluster CRUD operations.
 */
@Controller
@RequestMapping("/configuration/cluster")
public class ClusterConfigController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ClusterConfigController.class);

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private SecretManager secretManager;

    @Autowired
    private KafkaOperationsFactory kafkaOperationsFactory;

    @Autowired
    private SaslUtility saslUtility;

    @Autowired
    private SensitiveConfigScrubber sensitiveConfigScrubber;

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all clusters
        final Iterable<Cluster> clusterList = clusterRepository.findAllByOrderByNameAsc();
        model.addAttribute("clusterList", clusterList);

        return "configuration/cluster/index";
    }

    /**
     * GET Displays create cluster form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createClusterForm(final ClusterForm clusterForm, final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", "/configuration/cluster/create");
        setupCreateForm(model);

        return "configuration/cluster/create";
    }

    private void setupCreateForm(final Model model) {
        // Load all available properties
        model.addAttribute("kafkaSettings", KafkaClientConfigUtil.getAllKafkaConsumerProperties());
    }

    /**
     * GET Displays edit cluster form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editClusterForm(
        @PathVariable final Long id,
        final ClusterForm clusterForm,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Initial setup
        setupCreateForm(model);

        // Retrieve by id
        final Optional<Cluster> clusterOptional = clusterRepository.findById(id);
        if (!clusterOptional.isPresent()) {
            // redirect
            // Set flash message
            final FlashMessage flashMessage = FlashMessage.newWarning("Unable to find cluster!");
            redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

            // redirect to cluster index
            return "redirect:/configuration/cluster";
        }
        final Cluster cluster = clusterOptional.get();

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Edit: " + cluster.getName(), null);

        // Build form
        clusterForm.setId(cluster.getId());
        clusterForm.setName(cluster.getName());
        clusterForm.setBrokerHosts(cluster.getBrokerHosts());

        // Set SSL options
        clusterForm.setSsl(cluster.isSslEnabled());
        clusterForm.setKeyStoreFilename(cluster.getKeyStoreFile());
        clusterForm.setTrustStoreFilename(cluster.getTrustStoreFile());
        clusterForm.setUseTrustStore(clusterForm.hasTrustStoreFilename());

        // Set SASL options
        final SaslProperties saslProperties = saslUtility.decodeProperties(cluster);
        clusterForm.setSasl(cluster.isSaslEnabled());

        clusterForm.setSaslMechanism(cluster.getSaslMechanism());
        if (!cluster.getSaslMechanism().equals("PLAIN") && !cluster.getSaslMechanism().equals("GSSAPI")) {
            clusterForm.setSaslMechanism("custom");
        }
        clusterForm.setSaslCustomMechanism(cluster.getSaslMechanism());
        clusterForm.setSaslUsername(saslProperties.getPlainUsername());
        clusterForm.setSaslPassword(saslProperties.getPlainPassword());
        clusterForm.setSaslCustomJaas(saslProperties.getJaas());

        // Deserialize message parameters json string into a map
        final ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> customOptions;
        try {
            customOptions = objectMapper.readValue(cluster.getOptionParameters(), Map.class);
        } catch (final IOException e) {
            // Fail safe?
            customOptions = new HashMap<>();
        }

        // Update form object with properties.
        for (final Map.Entry<String, String> entry : customOptions.entrySet()) {
            clusterForm.getCustomOptionNames().add(entry.getKey());
            clusterForm.getCustomOptionValues().add(entry.getValue());
        }
        clusterForm.setCustomOptionsEnabled(!customOptions.entrySet().isEmpty());

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
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Initial Setup.
        setupCreateForm(model);

        final boolean updateExisting = clusterForm.exists();

        // Ensure that cluster name is not already used.
        final Cluster existingCluster = clusterRepository.findByName(clusterForm.getName());
        if (existingCluster != null) {
            // If we're updating, exclude our own id.
            if (!updateExisting
                || (updateExisting && !clusterForm.getId().equals(existingCluster.getId()))) {
                bindingResult.addError(new FieldError(
                    "clusterForm", "name", clusterForm.getName(), true, null, null, "Name is already used")
                );
            }
        }

        // If SSL is enabled
        if (clusterForm.getSsl()) {
            // If we're creating a new cluster
            if (!clusterForm.exists()) {
                // Ensure that we have files uploaded for the truststore,
                // but only if they elected to upload a truststore at all.
                if (clusterForm.getUseTrustStore()) {
                    if (clusterForm.getTrustStoreFile() == null || clusterForm.getTrustStoreFile().isEmpty()) {
                        bindingResult.addError(new FieldError(
                            "clusterForm", "trustStoreFile", null, true, null, null, "Select a TrustStore JKS to upload")
                        );
                    }
                }

                // Only require KeyStore if NOT using SASL
                if (!clusterForm.getSasl()) {
                    // If no keystore file provided, add validation error.
                    if (clusterForm.getKeyStoreFile() == null || clusterForm.getKeyStoreFile().isEmpty()) {
                        bindingResult.addError(new FieldError(
                            "clusterForm", "keyStoreFile", null, true, null, null, "Select a KeyStore JKS to upload")
                        );
                    }
                }
            }
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            return "configuration/cluster/create";
        }

        // If we're updating
        final Cluster cluster;
        final String successMessage;
        if (updateExisting) {
            // Retrieve it
            final Optional<Cluster> clusterOptional = clusterRepository.findById(clusterForm.getId());
            if (!clusterOptional.isPresent()) {
                // redirect
                // Set flash message
                final FlashMessage flashMessage = FlashMessage.newWarning("Unable to find cluster!");
                redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

                // redirect to cluster index
                return "redirect:/configuration/cluster";
            }
            cluster = clusterOptional.get();

            successMessage = "Updated cluster successfully!";
        } else {
            cluster = new Cluster();
            successMessage = "Created new cluster!";
        }

        // SSL Options
        if (clusterForm.getSsl()) {
            // Flip flag to true
            cluster.setSslEnabled(true);

            // If they've selected to NOT use a trust store.
            if (!clusterForm.getUseTrustStore()) {
                // Delete previous trust store if exists
                if (cluster.getTrustStoreFile() != null) {
                    uploadManager.deleteKeyStore(cluster.getTrustStoreFile());
                }
                // Clear out properties
                cluster.setTrustStoreFile(null);
                cluster.setTrustStorePassword(null);
            }

            /*
             * Determine if we should update truststore.  We Update it in the following scenarios:
             * - If the cluster is being newly created.
             * - If they uploaded a trust store, and there previously was a truststore.
             */
            else if (!clusterForm.exists() || (clusterForm.getTrustStoreFile() != null && !clusterForm.getTrustStoreFile().isEmpty())) {
                // Delete previous trust store if updating
                if (cluster.getTrustStoreFile() != null) {
                    uploadManager.deleteKeyStore(cluster.getTrustStoreFile());
                    cluster.setTrustStoreFile(null);
                    cluster.setTrustStorePassword(null);
                }

                // Sanitize filename
                final String filename = clusterForm.getName().replaceAll("[^A-Za-z0-9]", "_") + ".truststore.jks";

                // Persist JKS on filesystem
                try {
                    // Encrypt password
                    final String encrypted = secretManager.encrypt(clusterForm.getTrustStorePassword());

                    // Handle upload
                    uploadManager.handleKeystoreUpload(clusterForm.getTrustStoreFile(), filename);

                    // Persist in model.
                    cluster.setTrustStoreFile(filename);
                    cluster.setTrustStorePassword(encrypted);
                } catch (IOException exception) {
                    // TODO handle
                    throw new RuntimeException(exception.getMessage(), exception);
                }
            }

            // Determine if we should update keystores
            if (!clusterForm.exists() || (clusterForm.getKeyStoreFile() != null && !clusterForm.getKeyStoreFile().isEmpty())) {
                // Delete previous key store if updating, or if SASL is enabled.
                if (clusterForm.getSasl() || cluster.getKeyStoreFile() != null) {
                    uploadManager.deleteKeyStore(cluster.getKeyStoreFile());
                    cluster.setKeyStoreFile(null);
                    cluster.setKeyStorePassword(null);
                }

                // If not using SASL, handle upload of keystore.
                if (!clusterForm.getSasl()) {
                    // Sanitize filename
                    final String filename = clusterForm.getName().replaceAll("[^A-Za-z0-9]", "_") + ".keystore.jks";

                    // Persist JKS on filesystem
                    try {
                        // Encrypt password
                        final String encrypted = secretManager.encrypt(clusterForm.getKeyStorePassword());

                        // Handle upload
                        uploadManager.handleKeystoreUpload(clusterForm.getKeyStoreFile(), filename);

                        // Persist in model
                        cluster.setKeyStoreFile(filename);
                        cluster.setKeyStorePassword(encrypted);
                    } catch (IOException exception) {
                        // TODO handle
                        throw new RuntimeException(exception.getMessage(), exception);
                    }
                }
            }
        } else {
            // Disable SSL options
            cluster.setSslEnabled(false);

            // Remove from disk
            uploadManager.deleteKeyStore(cluster.getKeyStoreFile());
            uploadManager.deleteKeyStore(cluster.getTrustStoreFile());

            // Null out fields
            cluster.setKeyStoreFile(null);
            cluster.setKeyStorePassword(null);
            cluster.setTrustStoreFile(null);
            cluster.setTrustStorePassword(null);
        }

        // If sasl is enabled
        if (clusterForm.getSasl()) {
            // Flip enabled bit
            cluster.setSaslEnabled(true);

            // Build sasl properties
            final SaslProperties.Builder saslBuilder = SaslProperties.newBuilder();

            // Save mechanism
            if (clusterForm.isCustomSaslMechanism()) {
                saslBuilder.withMechanism(clusterForm.getSaslCustomMechanism());
            } else {
                saslBuilder.withMechanism(clusterForm.getSaslMechanism());
            }

            // If doing plain mechanism
            if (clusterForm.isPlainSaslMechanism()) {
                // Grab username and password
                saslBuilder
                    .withPlainUsername(clusterForm.getSaslUsername())
                    .withPlainPassword(clusterForm.getSaslPassword())
                    .withJaas("");
            } else {
                saslBuilder
                    .withJaas(clusterForm.getSaslCustomJaas());
            }

            // Build properties
            final SaslProperties saslProperties = saslBuilder.build();

            // Persist Properties into cluster instance.
            cluster.setSaslMechanism(saslProperties.getMechanism());
            cluster.setSaslConfig(saslUtility.encryptProperties(saslProperties));
        } else {
            // Clear sasl values.
            cluster.setSaslEnabled(false);
            cluster.setSaslMechanism("");
            cluster.setSaslConfig("");
        }

        // Handle custom options, convert into a JSON string.
        final String jsonStr = handleCustomOptions(clusterForm);

        // Update properties
        cluster.setName(clusterForm.getName());
        cluster.setBrokerHosts(clusterForm.getBrokerHosts());
        cluster.setOptionParameters(jsonStr);
        cluster.setValid(false);
        clusterRepository.save(cluster);

        // Set flash message
        final FlashMessage flashMessage = FlashMessage.newSuccess(successMessage);
        redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

        // redirect to cluster index
        return "redirect:/configuration/cluster";
    }

    /**
     * Handles getting custom defined options and values.
     * @param clusterForm The submitted form.
     */
    private String handleCustomOptions(final ClusterForm clusterForm) {
        // If the checkbox is unselected, then just return "{}"
        if (!clusterForm.getCustomOptionsEnabled()) {
            return "{}";
        }

        // Build a map of Name => Value
        final Map<String, String> mappedOptions = clusterForm.getCustomOptionsAsMap();

        // For converting map to json string
        final ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(mappedOptions);
        } catch (final JsonProcessingException e) {
            // Fail safe?
            return "{}";
        }
    }

    /**
     * POST deletes the selected cluster.
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String deleteCluster(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        final Optional<Cluster> clusterOptional = clusterRepository.findById(id);
        if (!clusterOptional.isPresent()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find cluster!"));
        } else {
            final Cluster cluster = clusterOptional.get();

            // Delete KeyStores
            if (cluster.getTrustStoreFile() != null) {
                uploadManager.deleteKeyStore(cluster.getTrustStoreFile());
            }
            if (cluster.getKeyStoreFile() != null) {
                uploadManager.deleteKeyStore(cluster.getKeyStoreFile());
            }

            // Delete it
            clusterRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Deleted cluster!"));
        }

        // redirect to cluster index
        return "redirect:/configuration/cluster";
    }

    /**
     * GET for testing if a cluster is configured correctly.
     */
    @RequestMapping(path = "/test/{id}", method = RequestMethod.GET)
    public String testCluster(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        final Optional<Cluster> clusterOptional = clusterRepository.findById(id);
        if (!clusterOptional.isPresent()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find cluster!"));

            // redirect to cluster index
            return "redirect:/configuration/cluster";
        }
        final Cluster cluster = clusterOptional.get();

        // Create new Operational Client
        try {
            try (final KafkaOperations kafkaOperations = kafkaOperationsFactory.create(cluster, getLoggedInUserId())) {
                logger.info("Cluster Nodes: {}", kafkaOperations.getClusterNodes());

                // If we made it this far, we should be AOK
                cluster.setValid(true);
                clusterRepository.save(cluster);

                // Set success msg
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Cluster configuration is valid!"));
            }
        } catch (final Exception e) {
            // Collect all reasons.
            String reason = e.getMessage();
            if (e instanceof TimeoutException) {
                reason = reason + " (This may indicate an authentication or connection problem)";
            }

            // Set error msg
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newDanger("Error connecting to cluster: " + reason, e)
            );

            // Mark as invalid
            cluster.setValid(false);
            clusterRepository.save(cluster);
        }


        // redirect to cluster index
        return "redirect:/configuration/cluster";
    }

    /**
     * GET for getting client configuration.
     */
    @RequestMapping(path = "/config/{id}", method = RequestMethod.GET)
    public String getClientConfig(
        @PathVariable final Long id,
        final RedirectAttributes redirectAttributes,
        final Model model
    ) {
        // Retrieve it
        final Optional<Cluster> clusterOptional = clusterRepository.findById(id);
        if (!clusterOptional.isPresent()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find cluster!"));

            // redirect to cluster index
            return "redirect:/configuration/cluster";
        }
        final Cluster cluster = clusterOptional.get();

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Client Config: " + cluster.getName(), null);

        // Generate configs with sensitive fields scrubbed.
        final Map<String, Object> configs = sensitiveConfigScrubber.filterSensitiveOptions(
            kafkaOperationsFactory.getConsumerConfig(cluster, getLoggedInUserId()),
            cluster
        )
            // Sort by key for easier display.
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        // Render
        model.addAttribute("configs", configs);
        model.addAttribute("cluster", cluster);

        // Render cluster config template
        return "configuration/cluster/config";
    }

    private void setupBreadCrumbs(final Model model, final String name, final String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
            .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb("Clusters", "/configuration/cluster");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Clusters", null);
        }

        // Add default trust store property.
        String defaultTrustStore = System.getProperty("javax.net.ssl.trustStore", "<JRE_HOME>/lib/security/cacerts");
        if (defaultTrustStore != null && defaultTrustStore.trim().isEmpty()) {
            defaultTrustStore = "<JRE_HOME>/lib/security/cacerts";
        }
        model.addAttribute("defaultTrustStore", defaultTrustStore);
    }
}