/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.controller.configuration.partitioningstrategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.serialization.Deserializer;
import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.configuration.partitioningstrategy.forms.PartitioningStrategyForm;
import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.UploadManager;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.LoaderException;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.PartitioningStrategy;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.sourcelab.kafka.webview.ui.repository.PartitioningStrategyRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Partitioning Strategy CRUD operations.
 */
@Controller
@RequestMapping("/configuration/partitionStrategy")
public class PartitioningStrategyController extends BaseController {

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private PluginFactory<Partitioner> partitionerLoader;

    @Autowired
    private PartitioningStrategyRepository partitioningStrategyRepository;

    /**
     * GET Displays main message format index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all default formats
        final Iterable<PartitioningStrategy> defaultEntries = partitioningStrategyRepository.findByIsDefaultOrderByNameAsc(true);

        // Retrieve all custom formats
        final Iterable<PartitioningStrategy> customEntries = partitioningStrategyRepository.findByIsDefaultOrderByNameAsc(false);

        // Set view attributes
        model.addAttribute("defaultEntries", defaultEntries);
        model.addAttribute("customEntries", customEntries);

        return "configuration/partitionStrategy/index";
    }

    /**
     * GET Displays create partitioning strategy form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createPartitionStrategy(final PartitioningStrategyForm PartitioningStrategyForm, final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", null);

        return "configuration/partitionStrategy/create";
    }

    /**
     * GET Displays edit partitioning strategy form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editPartitionStrategy(
        @PathVariable final Long id,
        final PartitioningStrategyForm PartitioningStrategyForm,
        final Model model,
        final RedirectAttributes redirectAttributes) {
        // Retrieve it
        final Optional<PartitioningStrategy> partitioningStrategyOptional = partitioningStrategyRepository.findById(id);
        if (!partitioningStrategyOptional.isPresent()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find partitioning strategy!"));
            return "redirect:/configuration/partitionStrategy";
        }
        final PartitioningStrategy partitioningStrategy = partitioningStrategyOptional.get();

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Edit " + partitioningStrategy.getName(), null);

        // Setup form
        PartitioningStrategyForm.setId(partitioningStrategy.getId());
        PartitioningStrategyForm.setName(partitioningStrategy.getName());
        PartitioningStrategyForm.setClasspath(partitioningStrategy.getClasspath());

        // Deserialize message parameters json string into a map
        final ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> customOptions;
        try {
            customOptions = objectMapper.readValue(partitioningStrategy.getOptionParameters(), Map.class);
        } catch (final IOException e) {
            // Fail safe?
            customOptions = new HashMap<>();
        }

        // Update form object with properties.
        for (final Map.Entry<String, String> entry : customOptions.entrySet()) {
            PartitioningStrategyForm.getCustomOptionNames().add(entry.getKey());
            PartitioningStrategyForm.getCustomOptionValues().add(entry.getValue());
        }

        return "configuration/partitionStrategy/create";
    }

    /**
     * POST create or edit existing Partitioning Strategy.
     *
     * If the partitioning strategy does NOT yet exist:
     *   - Require a valid JAR + Classpath to be uploaded
     *
     * If the partitioning strategy DOES exist
     *   - If no jar is uploaded, only allow updating the name + options
     *   - If jar is uploaded, validate JAR + Classpath
     *     - If valid, replace existing Jar
     *     - If not valid, keep existing Jar.
     *
     */
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public String create(
        @Valid final PartitioningStrategyForm PartitioningStrategyForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        @RequestParam final Map<String, String> allRequestParams) {

        // If we have errors just display the form again.
        if (bindingResult.hasErrors()) {
            return "configuration/partitionStrategy/create";
        }

        // Grab uploaded file
        final MultipartFile file = PartitioningStrategyForm.getFile();

        // If the partitioning strategy doesn't exist, and no file uploaded.
        if (!PartitioningStrategyForm.exists() && file.isEmpty()) {
            bindingResult.addError(new FieldError(
                "PartitioningStrategyForm", "file", "", true, null, null, "Select a jar to upload")
            );
            return "configuration/partitionStrategy/create";
        }

        // If Partitioning Strategy exists
        final PartitioningStrategy partitioningStrategy;
        if (PartitioningStrategyForm.exists()) {
            // Retrieve partitioning strategy
            final Optional<PartitioningStrategy> partitioningStrategyOptional = partitioningStrategyRepository.findById(PartitioningStrategyForm.getId());

            // If we can't find the entry
            if (!partitioningStrategyOptional.isPresent()) {
                // Set flash message & redirect
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find partitioning strategy!"));
                return "redirect:/configuration/partitionStrategy";
            }
            partitioningStrategy = partitioningStrategyOptional.get();
        } else {
            // Creating new partitioning strategy
            partitioningStrategy = new PartitioningStrategy();
        }

        // Handle custom options, convert into a JSON string.
        final String jsonStr = handleCustomOptions(PartitioningStrategyForm);

        // If we have a new file uploaded.
        if (!file.isEmpty()) {
            try {
                // Sanitize file name.
                final String newFilename = PartitioningStrategyForm.getName().replaceAll("[^A-Za-z0-9]", "_") + ".jar";
                final String tempFilename = newFilename + ".tmp";

                // Persist jar on filesystem in a temporary location
                final String jarPath = uploadManager.handlePartitioningStrategyUpload(file, tempFilename);

                // Attempt to load jar?
                try {
                    partitionerLoader.getPlugin(tempFilename, PartitioningStrategyForm.getClasspath());
                } catch (final LoaderException exception) {
                    // If we had issues, remove the temp location
                    Files.delete(Paths.get(jarPath));

                    // Add an error
                    bindingResult.addError(new FieldError(
                        "PartitioningStrategyForm", "file", "", true, null, null, exception.getMessage())
                    );
                    // And re-display the form.
                    return "configuration/partitionStrategy/create";
                }
                // Ok new JAR looks good.
                // 1 - remove pre-existing jar if it exists
                if (partitioningStrategy.getJar() != null && !partitioningStrategy.getJar().isEmpty()) {
                    // Delete pre-existing jar.
                    Files.deleteIfExists(partitionerLoader.getPathForJar(partitioningStrategy.getJar()));
                }

                // 2 - move tempFilename => filename.
                // Lets just delete the temp path and re-handle the upload.
                Files.deleteIfExists(Paths.get(jarPath));
                uploadManager.handlePartitioningStrategyUpload(file, newFilename);

                // 3 - Update the jar and class path properties.
                partitioningStrategy.setJar(newFilename);
                partitioningStrategy.setClasspath(PartitioningStrategyForm.getClasspath());
            } catch (final IOException e) {
                // Set flash message
                redirectAttributes.addFlashAttribute("exception", e.getMessage());
                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newWarning("Unable to save uploaded JAR: " + e.getMessage()));

                // redirect to cluster index
                return "redirect:/configuration/partitionStrategy";
            }
        }

        // If we made it here, write MessageFormat entity.
        partitioningStrategy.setName(PartitioningStrategyForm.getName());
        partitioningStrategy.setDefault(false);
        partitioningStrategy.setOptionParameters(jsonStr);
        partitioningStrategyRepository.save(partitioningStrategy);

        redirectAttributes.addFlashAttribute(
            "FlashMessage",
            FlashMessage.newSuccess("Successfully created partitioning strategy!"));
        return "redirect:/configuration/partitionStrategy";
    }

    /**
     * Handles getting custom defined options and values.
     * @param PartitioningStrategyForm The submitted form.
     */
    private String handleCustomOptions(final PartitioningStrategyForm PartitioningStrategyForm) {
        // Build a map of Name => Value
        final Map<String, String> mappedOptions = PartitioningStrategyForm.getCustomOptionsAsMap();

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
     * POST deletes the selected partitioning strategy.
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String deletePartitioningStrategy(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        // Where to redirect.
        final String redirectUrl = "redirect:/configuration/partitionStrategy";

        // Retrieve it
        final Optional<PartitioningStrategy> partitioningStrategyOptional = partitioningStrategyRepository.findById(id);
        if (!partitioningStrategyOptional.isPresent() || partitioningStrategyOptional.get().isDefault()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newWarning("Unable to remove partitioning strategy!"));
            return redirectUrl;
        }
        final PartitioningStrategy partitioningStrategy = partitioningStrategyOptional.get();

        // See if its in use by any producer templates
        // TODO

        try {
            // Delete entity
            partitioningStrategyRepository.deleteById(id);

            // Delete jar from disk
            try {
                Files.deleteIfExists(partitionerLoader.getPathForJar(partitioningStrategy.getJar()));
            } catch (final NoSuchFileException exception) {
                // swallow.
            }
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newSuccess("Deleted partitioning strategy!"));
        } catch (final IOException e) {
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newWarning("Unable to remove partitioning strategy! " + e.getMessage()));
            return redirectUrl;
        }

        // redirect to cluster index
        return redirectUrl;
    }

    private void setupBreadCrumbs(final Model model, final String name, final String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
            .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb("Partitioning Strategies", "/configuration/partitioningStrategy");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Partitioning Strategies", null);
        }
    }
}
