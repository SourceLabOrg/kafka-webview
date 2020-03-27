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

package org.sourcelab.kafka.webview.ui.manager.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.UploadManager;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.LoaderException;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.UploadableJarEntity;
import org.sourcelab.kafka.webview.ui.repository.UploadableJarRepository;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

/**
 * Abstracted out common controller code for UploadableJarEntity instances.
 *
 * @param <EntityT> The entity the controller is performing operations on.
 */
public class UploadableJarControllerHelper<EntityT extends UploadableJarEntity> {

    /**
     * Dependencies.
     */
    private final UploadManager uploadManager;
    private final PluginFactory<?> pluginFactory;
    private final UploadableJarRepository<EntityT> entityRepository;

    /**
     * Configuration.
     */
    private final String moduleName;
    private final String entityDisplayNameSingular;
    private final String entityDisplayNamePlural;
    private final Class<EntityT> entityClass;

    /**
     * Constructor.
     * @param entityDisplayNameSingular Display name of entity, in singular form.  Example "Message Format"
     * @param entityDisplayNamePlural Display name of the entity, in plural form.  Example "Message Formats"
     * @param moduleName path of the module in the UI.  example: "messageFormat"
     * @param entityClass Class of the entity.
     * @param uploadManager UploadManager instance.
     * @param pluginFactory PluginFactory instance.
     * @param entityRepository EntityT's repository instance.
     */
    public UploadableJarControllerHelper(
        final String entityDisplayNameSingular,
        final String entityDisplayNamePlural,
        final String moduleName,
        final Class<EntityT> entityClass,
        final UploadManager uploadManager,
        final PluginFactory<?> pluginFactory,
        final UploadableJarRepository<EntityT> entityRepository) {

        this.uploadManager = uploadManager;
        this.pluginFactory = pluginFactory;
        this.entityRepository = entityRepository;
        this.moduleName = moduleName;
        this.entityDisplayNameSingular = entityDisplayNameSingular;
        this.entityDisplayNamePlural = entityDisplayNamePlural;
        this.entityClass = entityClass;
    }

    /**
     * Build the controllers index response.
     * @param model controller's model instance.
     * @return Controller response.
     */
    public String buildIndex(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", null);

        // Retrieve all default formats
        final Iterable<EntityT> defaultEntries = entityRepository.findByIsDefaultOrderByNameAsc(true);

        // Retrieve all custom formats
        final Iterable<EntityT> customEntries = entityRepository.findByIsDefaultOrderByNameAsc(false);

        // Set view attributes
        model.addAttribute("defaultEntries", defaultEntries);
        model.addAttribute("customEntries", customEntries);

        return this.moduleName + "/index";
    }

    /**
     * Build the controllers create response.
     * @param model controller's model instance.
     * @return Controller response.
     */
    public String buildCreate(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", null);

        return this.moduleName + "/create";
    }

    /**
     * Build the controllers edit response.
     * @param id id of entity
     * @param form Controller's form instance.
     * @param model Controller's model instance.
     * @param redirectAttributes Controller's redirectAttributes instance.
     * @return Controller response.
     */
    public String buildEdit(
        final long id,
        final UploadableJarForm form,
        final Model model,
        final RedirectAttributes redirectAttributes
    ) {

        // Retrieve it
        final Optional<EntityT> entityOptional = entityRepository.findById(id);
        if (!entityOptional.isPresent()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newWarning("Unable to find " + this.entityDisplayNameSingular.toLowerCase() + "!")
            );
            return "redirect:/" + this.moduleName;
        }
        final EntityT entity = entityOptional.get();

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Edit " + entity.getName(), null);

        // Setup form
        form.setId(entity.getId());
        form.setName(entity.getName());
        form.setClasspath(entity.getClasspath());

        // Deserialize message parameters json string into a map
        final ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> customOptions;
        try {
            customOptions = objectMapper.readValue(entity.getOptionParameters(), Map.class);
        } catch (final IOException e) {
            // Fail safe?
            customOptions = new HashMap<>();
        }

        // Update form object with properties.
        for (final Map.Entry<String, String> entry : customOptions.entrySet()) {
            form.getCustomOptionNames().add(entry.getKey());
            form.getCustomOptionValues().add(entry.getValue());
        }

        return this.moduleName + "/create";
    }

    /**
     * Handle processing controller's update response.
     * @param form Controller's form instance.
     * @param bindingResult Controller's binding result instance.
     * @param redirectAttributes Controller's redirectAttributes instance.
     * @return Controller response.
     */
    public String handleUpdate(
        final UploadableJarForm form,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes
    ) {

        // If we have errors just display the form again.
        if (bindingResult.hasErrors()) {
            return this.moduleName + "/create";
        }

        // Grab uploaded file
        final MultipartFile file = form.getFile();

        // If the partitioning strategy doesn't exist, and no file uploaded.
        if (!form.exists() && file.isEmpty()) {
            bindingResult.addError(new FieldError(
                bindingResult.getObjectName(), "file", "", true, null, null, "Select a jar to upload")
            );
            return this.moduleName + "/create";
        }

        // If Partitioning Strategy exists
        final EntityT entity;
        if (form.exists()) {
            // Retrieve partitioning strategy
            final Optional<EntityT> entityOptional = entityRepository.findById(form.getId());

            // If we can't find the entry
            if (!entityOptional.isPresent()) {
                // Set flash message & redirect
                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newWarning("Unable to find " + entityDisplayNameSingular.toLowerCase() + "!")
                );
                return "redirect:/" + this.moduleName;
            }
            entity = entityOptional.get();
        } else {
            // Creating new partitioning strategy
            try {
                entity = entityClass.getDeclaredConstructor().newInstance();
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        // Handle custom options, convert into a JSON string.
        final String jsonStr = handleCustomOptions(form);

        // If we have a new file uploaded.
        if (!file.isEmpty()) {
            try {
                // Sanitize file name.
                final String newFilename = form.getName().replaceAll("[^A-Za-z0-9]", "_") + ".jar";
                final String tempFilename = newFilename + ".tmp";

                // Persist jar on filesystem in a temporary location
                final String jarPath = uploadManager.handleUpload(file, tempFilename, entity.getUploadType());

                // Attempt to load jar?
                try {
                    pluginFactory.checkPlugin(tempFilename, form.getClasspath());
                } catch (final LoaderException exception) {
                    // If we had issues, remove the temp location
                    Files.delete(Paths.get(jarPath));

                    // Add an error
                    bindingResult.addError(new FieldError(
                        bindingResult.getObjectName(), "file", "", true, null, null, exception.getMessage())
                    );
                    // And re-display the form.
                    return this.moduleName + "/create";
                }
                // Ok new JAR looks good.
                // 1 - remove pre-existing jar if it exists
                if (entity.getJar() != null && !entity.getJar().isEmpty()) {
                    // Delete pre-existing jar.
                    Files.deleteIfExists(pluginFactory.getPathForJar(entity.getJar()));
                }

                // 2 - move tempFilename => filename.
                // Lets just delete the temp path and re-handle the upload.
                Files.deleteIfExists(Paths.get(jarPath));
                uploadManager.handleUpload(file, newFilename, entity.getUploadType());

                // 3 - Update the jar and class path properties.
                entity.setJar(newFilename);
                entity.setClasspath(form.getClasspath());
            } catch (final IOException e) {
                // Set flash message
                redirectAttributes.addFlashAttribute("exception", e.getMessage());
                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newWarning("Unable to save uploaded JAR: " + e.getMessage()));

                // redirect to cluster index
                return "redirect:/" + this.moduleName;
            }
        }

        // If we made it here, write MessageFormat entity.
        entity.setName(form.getName());
        entity.setDefault(false);
        entity.setOptionParameters(jsonStr);
        entityRepository.save(entity);

        redirectAttributes.addFlashAttribute(
            "FlashMessage",
            FlashMessage.newSuccess("Successfully created " + this.entityDisplayNameSingular.toLowerCase() + "!"));
        return "redirect:/" + this.moduleName;
    }

    /**
     * Process a delete request for a UploadableJarEntity controller.
     * @param id of entity to process delete for.
     * @param redirectAttributes Controllers redirect attributes.
     * @param entityUsageManager Implementation for finding usages of this entity.
     * @return controller response.
     */
    public String processDelete(
        final Long id,
        final RedirectAttributes redirectAttributes,
        final EntityUsageManager entityUsageManager
    ) {
        // Where to redirect.
        final String redirectUrl = "redirect:/" + this.moduleName;

        // Retrieve it
        final Optional<EntityT> entityOptional = entityRepository.findById(id);
        if (!entityOptional.isPresent() || entityOptional.get().isDefault()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newWarning("Unable to remove " + this.entityDisplayNameSingular + "!"));
            return redirectUrl;
        }
        final EntityT entity = entityOptional.get();

        // See if its in use by anything.
        final Collection<EntityUsageManager.Usage> usages = entityUsageManager.findUsages(id);
        if (!usages.isEmpty()) {
            // Build message
            String errorMessage = this.entityDisplayNameSingular + " in use by ";
            Collection<String> errorMsgUsages = new HashSet<>();
            for (final EntityUsageManager.Usage usage : usages) {
                errorMsgUsages.add(usage.toString());
            }

            // Set flash message & redirect
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newWarning(errorMessage + String.join(", ", errorMsgUsages)));
            return redirectUrl;
        }

        try {
            // Delete entity
            entityRepository.deleteById(id);

            // Delete jar from disk
            try {
                Files.deleteIfExists(pluginFactory.getPathForJar(entity.getJar()));
            } catch (final NoSuchFileException exception) {
                // swallow.
            }
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newSuccess("Deleted " + this.entityDisplayNameSingular + "!"));
        } catch (final IOException e) {
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newWarning("Unable to remove " + this.entityDisplayNameSingular + "! " + e.getMessage()));
            return redirectUrl;
        }

        // redirect to cluster index
        return redirectUrl;
    }

    /**
     * Handles getting custom defined options and values.
     * @param form The submitted form.
     */
    private String handleCustomOptions(final UploadableJarForm form) {
        // Build a map of Name => Value
        final Map<String, String> mappedOptions = form.getCustomOptionsAsMap();

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
     * Sets up breadcrumbs in UI.
     * @param model controller model instance.
     * @param name name of entity, or null.
     * @param url Optional URL to display.
     */
    private void setupBreadCrumbs(final Model model, final String name, final String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
            .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb(entityDisplayNamePlural, "/" + this.moduleName);
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb(entityDisplayNamePlural, null);
        }
    }
}
