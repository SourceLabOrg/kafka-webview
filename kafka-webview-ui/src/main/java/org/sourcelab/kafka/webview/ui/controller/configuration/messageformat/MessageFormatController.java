/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.controller.configuration.messageformat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.configuration.messageformat.forms.MessageFormatForm;
import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.UploadManager;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.LoaderException;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
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
 * Controller for MessageFormat CRUD operations.
 */
@Controller
@RequestMapping("/configuration/messageFormat")
public class MessageFormatController extends BaseController {

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private PluginFactory<Deserializer> deserializerLoader;

    @Autowired
    private MessageFormatRepository messageFormatRepository;

    @Autowired
    private ViewRepository viewRepository;

    /**
     * GET Displays main message format index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all default formats
        final Iterable<MessageFormat> defaultMessageFormats = messageFormatRepository.findByIsDefaultFormatOrderByNameAsc(true);

        // Retrieve all custom formats
        final Iterable<MessageFormat> customMessageFormats = messageFormatRepository.findByIsDefaultFormatOrderByNameAsc(false);

        // Set view attributes
        model.addAttribute("defaultMessageFormats", defaultMessageFormats);
        model.addAttribute("customMessageFormats", customMessageFormats);

        return "configuration/messageFormat/index";
    }

    /**
     * GET Displays create message format form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createMessageFormat(final MessageFormatForm messageFormatForm, final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", null);

        return "configuration/messageFormat/create";
    }

    /**
     * GET Displays edit message format form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editMessageFormat(
        @PathVariable final Long id,
        final MessageFormatForm messageFormatForm,
        final Model model,
        final RedirectAttributes redirectAttributes) {
        // Retrieve it
        final Optional<MessageFormat> messageFormatOptional = messageFormatRepository.findById(id);
        if (!messageFormatOptional.isPresent()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find message format!"));
            return "redirect:/configuration/messageFormat";
        }
        final MessageFormat messageFormat = messageFormatOptional.get();

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Edit " + messageFormat.getName(), null);

        // Setup form
        messageFormatForm.setId(messageFormat.getId());
        messageFormatForm.setName(messageFormat.getName());
        messageFormatForm.setClasspath(messageFormat.getClasspath());

        // Deserialize message parameters json string into a map
        final ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> customOptions;
        try {
            customOptions = objectMapper.readValue(messageFormat.getOptionParameters(), Map.class);
        } catch (final IOException e) {
            // Fail safe?
            customOptions = new HashMap<>();
        }

        // Update form object with properties.
        for (final Map.Entry<String, String> entry : customOptions.entrySet()) {
            messageFormatForm.getCustomOptionNames().add(entry.getKey());
            messageFormatForm.getCustomOptionValues().add(entry.getValue());
        }

        return "configuration/messageFormat/create";
    }

    /**
     * POST create or edit existing MessageFormat.
     *
     * If the message format does NOT yet exist:
     *   - Require a valid JAR + Classpath to be uploaded
     *
     * If the message format DOES exist
     *   - If no jar is uploaded, only allow updating the name + options
     *   - If jar is uploaded, validate JAR + Classpath
     *     - If valid, replace existing Jar
     *     - If not valid, keep existing Jar.
     *
     */
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public String create(
        @Valid final MessageFormatForm messageFormatForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        @RequestParam final Map<String, String> allRequestParams) {

        // If we have errors just display the form again.
        if (bindingResult.hasErrors()) {
            return "configuration/messageFormat/create";
        }

        // Grab uploaded file
        final MultipartFile file = messageFormatForm.getFile();

        // If the message format doesn't exist, and no file uploaded.
        if (!messageFormatForm.exists() && file.isEmpty()) {
            bindingResult.addError(new FieldError(
                "messageFormatForm", "file", "", true, null, null, "Select a jar to upload")
            );
            return "configuration/messageFormat/create";
        }

        // If filter exists
        final MessageFormat messageFormat;
        if (messageFormatForm.exists()) {
            // Retrieve message format
            final Optional<MessageFormat> messageFormatOptional = messageFormatRepository.findById(messageFormatForm.getId());

            // If we can't find the format
            if (!messageFormatOptional.isPresent()) {
                // Set flash message & redirect
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find message format!"));
                return "redirect:/configuration/messageFormat";
            }
            messageFormat = messageFormatOptional.get();
        } else {
            // Creating new message format
            messageFormat = new MessageFormat();
        }

        // Handle custom options, convert into a JSON string.
        final String jsonStr = handleCustomOptions(messageFormatForm);

        // If we have a new file uploaded.
        if (!file.isEmpty()) {
            try {
                // Sanitize file name.
                final String newFilename = messageFormatForm.getName().replaceAll("[^A-Za-z0-9]", "_") + ".jar";
                final String tempFilename = newFilename + ".tmp";

                // Persist jar on filesystem in a temporary location
                final String jarPath = uploadManager.handleDeserializerUpload(file, tempFilename);

                // Attempt to load jar?
                try {
                    deserializerLoader.checkPlugin(tempFilename, messageFormatForm.getClasspath());
                } catch (final LoaderException exception) {
                    // If we had issues, remove the temp location
                    Files.delete(Paths.get(jarPath));

                    // Add an error
                    bindingResult.addError(new FieldError(
                        "messageFormatForm", "file", "", true, null, null, exception.getMessage())
                    );
                    // And re-display the form.
                    return "configuration/messageFormat/create";
                }
                // Ok new JAR looks good.
                // 1 - remove pre-existing jar if it exists
                if (messageFormat.getJar() != null && !messageFormat.getJar().isEmpty()) {
                    // Delete pre-existing jar.
                    Files.deleteIfExists(deserializerLoader.getPathForJar(messageFormat.getJar()));
                }

                // 2 - move tempFilename => filename.
                // Lets just delete the temp path and re-handle the upload.
                Files.deleteIfExists(Paths.get(jarPath));
                uploadManager.handleDeserializerUpload(file, newFilename);

                // 3 - Update the jar and class path properties.
                messageFormat.setJar(newFilename);
                messageFormat.setClasspath(messageFormatForm.getClasspath());
            } catch (final IOException e) {
                // Set flash message
                redirectAttributes.addFlashAttribute("exception", e.getMessage());
                redirectAttributes.addFlashAttribute(
                    "FlashMessage",
                    FlashMessage.newWarning("Unable to save uploaded JAR: " + e.getMessage()));

                // redirect to cluster index
                return "redirect:/configuration/messageFormat";
            }
        }

        // If we made it here, write MessageFormat entity.
        messageFormat.setName(messageFormatForm.getName());
        messageFormat.setDefaultFormat(false);
        messageFormat.setOptionParameters(jsonStr);
        messageFormatRepository.save(messageFormat);

        redirectAttributes.addFlashAttribute(
            "FlashMessage",
            FlashMessage.newSuccess("Successfully created message format!"));
        return "redirect:/configuration/messageFormat";
    }

    /**
     * Handles getting custom defined options and values.
     * @param messageFormatForm The submitted form.
     */
    private String handleCustomOptions(final MessageFormatForm messageFormatForm) {
        // Build a map of Name => Value
        final Map<String, String> mappedOptions = messageFormatForm.getCustomOptionsAsMap();

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
     * POST deletes the selected message format.
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String deleteCluster(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        // Where to redirect.
        final String redirectUrl = "redirect:/configuration/messageFormat";

        // Retrieve it
        final Optional<MessageFormat> messageFormatOptional = messageFormatRepository.findById(id);
        if (!messageFormatOptional.isPresent() || messageFormatOptional.get().isDefaultFormat()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newWarning("Unable to remove message format!"));
            return redirectUrl;
        }
        final MessageFormat messageFormat = messageFormatOptional.get();

        // See if its in use by any views
        final Iterable<View> views = viewRepository
            .findAllByKeyMessageFormatIdOrValueMessageFormatIdOrderByNameAsc(messageFormat.getId(), messageFormat.getId());
        final Collection<String> viewNames = new ArrayList<>();
        for (final View view: views) {
            viewNames.add(view.getName());
        }
        if (!viewNames.isEmpty()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newWarning("Message format in use by views: " + viewNames.toString()));
            return redirectUrl;
        }

        try {
            // Delete entity
            messageFormatRepository.deleteById(id);

            // Delete jar from disk
            try {
                Files.deleteIfExists(deserializerLoader.getPathForJar(messageFormat.getJar()));
            } catch (final NoSuchFileException exception) {
                // swallow.
            }
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newSuccess("Deleted message format!"));
        } catch (final IOException e) {
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newWarning("Unable to remove message format! " + e.getMessage()));
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
            manager.addCrumb("Message Formats", "/configuration/messageFormat");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Message Formats", null);
        }
    }
}
