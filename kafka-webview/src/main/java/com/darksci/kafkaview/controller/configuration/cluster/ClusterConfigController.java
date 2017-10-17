package com.darksci.kafkaview.controller.configuration.cluster;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.controller.configuration.cluster.forms.ClusterForm;
import com.darksci.kafkaview.manager.kafka.KafkaAdminFactory;
import com.darksci.kafkaview.manager.kafka.KafkaOperations;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.plugin.UploadManager;
import com.darksci.kafkaview.manager.ui.BreadCrumbManager;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.repository.ClusterRepository;
import org.apache.kafka.clients.admin.AdminClient;
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
import java.io.IOException;

@Controller
@RequestMapping("/configuration/cluster")
public class ClusterConfigController extends BaseController {
    private final static Logger logger = LoggerFactory.getLogger(ClusterConfigController.class);

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private KafkaAdminFactory kafkaAdminFactory;

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all clusters
        final Iterable<Cluster> clusterList = clusterRepository.findAll();
        model.addAttribute("clusterList", clusterList);

        return "configuration/cluster/index";
    }

    /**
     * GET Displays createConsumer cluster form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createClusterForm(final ClusterForm clusterForm, final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", "/configuration/cluster/create");

        return "configuration/cluster/create";
    }

    /**
     * GET Displays edit cluster form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editClusterForm(
        final @PathVariable Long id,
        final ClusterForm clusterForm,
        final RedirectAttributes redirectAttributes,
        final Model model) {

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

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Edit: " + cluster.getName(), null);

        // Build form
        clusterForm.setId(cluster.getId());
        clusterForm.setName(cluster.getName());
        clusterForm.setBrokerHosts(cluster.getBrokerHosts());
        clusterForm.setSsl(cluster.isSslEnabled());
        clusterForm.setKeyStoreFilename(cluster.getKeyStoreFile());
        clusterForm.setTrustStoreFilename(cluster.getTrustStoreFile());

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

        // If SSL is enabled
        if (clusterForm.getSsl()) {
            // If we're creating a new cluster
            if (!clusterForm.exists()) {
                // Ensure that we have files uploaded
                if (clusterForm.getTrustStoreFile().isEmpty()) {
                    bindingResult.addError(new FieldError(
                        "clusterForm", "trustStoreFile", null, true, null, null, "Select a TrustStore JKS to upload")
                    );
                }
                if (clusterForm.getKeyStoreFile().isEmpty()) {
                    bindingResult.addError(new FieldError(
                        "clusterForm", "keyStoreFile", null, true, null, null, "Select a KeyStore JKS to upload")
                    );
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

        // SSL Options
        if (clusterForm.getSsl()) {
            // Flip flag to true
            cluster.setSslEnabled(true);

            // Determine if we should update keystores
            if (!clusterForm.exists() || !clusterForm.getTrustStoreFile().isEmpty()) {
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
                    uploadManager.handleKeystoreUpload(clusterForm.getTrustStoreFile(), filename);
                    cluster.setTrustStoreFile(filename);

                    // TODO encrypt this value
                    cluster.setTrustStorePassword(clusterForm.getTrustStorePassword());
                } catch (IOException exception) {
                    // TODO handle
                    throw new RuntimeException(exception.getMessage(), exception);
                }
            }

            // Handle key store update
            if (!clusterForm.exists() || !clusterForm.getKeyStoreFile().isEmpty()) {
                // Delete previous key store if updating
                if (cluster.getKeyStoreFile() != null) {
                    uploadManager.deleteKeyStore(cluster.getKeyStoreFile());
                    cluster.setKeyStoreFile(null);
                    cluster.setKeyStorePassword(null);
                }

                // Sanitize filename
                final String filename = clusterForm.getName().replaceAll("[^A-Za-z0-9]", "_") + ".keystore.jks";

                // Persist JKS on filesystem
                try {
                    uploadManager.handleKeystoreUpload(clusterForm.getKeyStoreFile(), filename);
                    cluster.setKeyStoreFile(filename);

                    // TODO encrypt this value
                    cluster.setKeyStorePassword(clusterForm.getKeyStorePassword());
                } catch (IOException exception) {
                    // TODO handle
                    throw new RuntimeException(exception.getMessage(), exception);
                }
            }
        } else {
            // Disable SSL options
            cluster.setSslEnabled(false);

            // TODO handle removing keystores
            cluster.setKeyStoreFile(null);
            cluster.setKeyStorePassword(null);
            cluster.setTrustStoreFile(null);
            cluster.setTrustStorePassword(null);
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
            // Delete KeyStores
            if (cluster.getTrustStoreFile() != null) {
                uploadManager.deleteKeyStore(cluster.getTrustStoreFile());
            }
            if (cluster.getKeyStoreFile() != null) {
                uploadManager.deleteKeyStore(cluster.getKeyStoreFile());
            }

            // Delete it
            clusterRepository.delete(id);

            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Deleted cluster!"));
        }

        // redirect to cluster index
        return "redirect:/configuration/cluster";
    }

    @RequestMapping(path = "/test/{id}", method = RequestMethod.GET)
    public String testCluster(final @PathVariable Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find cluster!"));
        }

        // Build a client
        // TODO use a clientId unique to the client + cluster + topic
        final String clientId = "TestingClient-" + cluster.getId();

        // Create new Operational Client
        final ClusterConfig.Builder clusterConfigBuilder = ClusterConfig.newBuilder(cluster);

        final AdminClient adminClient = kafkaAdminFactory.create(clusterConfigBuilder.build(), clientId);
        try (final KafkaOperations kafkaOperations = new KafkaOperations(adminClient)) {
            logger.info("Cluster Nodes: {}", kafkaOperations.getClusterNodes());

            // If we made it this far, we should be AOK
            cluster.setValid(true);
            clusterRepository.save(cluster);

            // Set success msg
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Cluster configuration is valid!"));
        } catch (Exception e) {
            // Set error msg
            cluster.setValid(false);
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newDanger("Error connecting to cluster: " + e.getMessage()));
        }
        // Update
        clusterRepository.save(cluster);

        // redirect to cluster index
        return "redirect:/configuration/cluster";
    }

    private void setupBreadCrumbs(final Model model, String name, String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
            .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb("Clusters", "/configuration/cluster");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Clusters", null);
        }
    }
}