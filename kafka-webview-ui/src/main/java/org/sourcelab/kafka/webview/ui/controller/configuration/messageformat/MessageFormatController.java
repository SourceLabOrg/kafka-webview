package org.sourcelab.kafka.webview.ui.controller.configuration.messageformat;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
     * POST create new MessageFormat.
     */
    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public String create(
        @Valid final MessageFormatForm messageFormatForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes) {

        // If we have errors just display the form again.
        if (bindingResult.hasErrors()) {
            return "configuration/messageFormat/create";
        }

        final MultipartFile file = messageFormatForm.getFile();
        if (file.isEmpty()) {
            bindingResult.addError(new FieldError(
                "messageFormatForm", "file", "", true, null, null, "Select a jar to upload")
            );
            return "/configuration/messageFormat/create";
        }

        // Make sure ends with .jar
        if (!file.getOriginalFilename().endsWith(".jar")) {
            bindingResult.addError(new FieldError(
                "messageFormatForm", "file", "", true, null, null, "File must have a .jar extension")
            );
            return "/configuration/messageFormat/create";
        }

        try {
            // Sanitize file name.
            final String filename = messageFormatForm.getName().replaceAll("[^A-Za-z0-9]", "_") + ".jar";

            // Persist jar on filesystem
            final String jarPath = uploadManager.handleDeserializerUpload(file, filename);

            // Attempt to load jar?
            try {
                deserializerLoader.getPlugin(filename, messageFormatForm.getClasspath());
            } catch (LoaderException e) {
                // Remove jar
                Files.delete(new File(jarPath).toPath());

                bindingResult.addError(new FieldError(
                    "messageFormatForm", "file", "", true, null, null, e.getMessage())
                );
                return "/configuration/messageFormat/create";
            }

            final MessageFormat messageFormat = new MessageFormat();
            messageFormat.setName(messageFormatForm.getName());
            messageFormat.setClasspath(messageFormatForm.getClasspath());
            messageFormat.setJar(filename);
            messageFormat.setDefaultFormat(false);
            messageFormatRepository.save(messageFormat);
        } catch (IOException e) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to save uploaded JAR: " + e.getMessage()));

            // redirect to cluster index
            return "redirect:/configuration/messageFormat";
        }

        // If we made it here, write new MessageFormat entity.
        redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Successfully created message format!"));
        return "redirect:/configuration/messageFormat";
    }

    /**
     * POST deletes the selected message format.
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String deleteCluster(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        // Where to redirect.
        final String redirectUrl = "redirect:/configuration/messageFormat";

        // Retrieve it
        final MessageFormat messageFormat = messageFormatRepository.findOne(id);
        if (messageFormat == null || messageFormat.isDefaultFormat()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newWarning("Unable to remove message format!"));
            return redirectUrl;
        }
        // See if its in use by any views
        final Iterable<View> views = viewRepository
            .findAllByKeyMessageFormatIdOrValueMessageFormatIdOrderByNameAsc(messageFormat.getId(), messageFormat.getId());
        final List<String> viewNames = new ArrayList<>();
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
            messageFormatRepository.delete(id);

            // Delete jar from disk
            Files.delete(deserializerLoader.getPathForJar(messageFormat.getJar()));
            redirectAttributes.addFlashAttribute(
                "FlashMessage",
                FlashMessage.newSuccess("Deleted message format!"));

        } catch (IOException e) {
            e.printStackTrace();
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
