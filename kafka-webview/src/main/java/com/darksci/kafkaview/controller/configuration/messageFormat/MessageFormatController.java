package com.darksci.kafkaview.controller.configuration.messageFormat;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.controller.configuration.messageFormat.forms.MessageFormatForm;
import com.darksci.kafkaview.manager.plugin.DeserializerLoader;
import com.darksci.kafkaview.manager.plugin.UploadManager;
import com.darksci.kafkaview.manager.plugin.exception.LoaderException;
import com.darksci.kafkaview.manager.ui.BreadCrumbManager;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.MessageFormat;
import com.darksci.kafkaview.repository.MessageFormatRepository;
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

@Controller
@RequestMapping("/configuration/messageFormat")
public class MessageFormatController extends BaseController {

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private DeserializerLoader deserializerLoader;

    @Autowired
    private MessageFormatRepository messageFormatRepository;

    /**
     * GET Displays main message format index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all message formats
        final Iterable<MessageFormat> messageFormatList = messageFormatRepository.findAllByOrderByNameAsc();
        model.addAttribute("messageFormats", messageFormatList);

        return "configuration/messageFormat/index";
    }

    /**
     * GET Displays createConsumer cluster form.
     */
    @RequestMapping(path = "/createConsumer", method = RequestMethod.GET)
    public String createMessageFormat(final MessageFormatForm messageFormatForm, final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", null);

        return "configuration/messageFormat/createConsumer";
    }

    @RequestMapping(path = "/createConsumer", method = RequestMethod.POST)
    public String create(
        @Valid final MessageFormatForm messageFormatForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes) {

        // If we have errors just display the form again.
        if (bindingResult.hasErrors()) {
            return "configuration/messageFormat/createConsumer";
        }

        final MultipartFile file = messageFormatForm.getFile();
        if (file.isEmpty()) {
            bindingResult.addError(new FieldError(
                "messageFormatForm", "file", "", true, null, null, "Select a jar to upload")
            );
            return "/configuration/messageFormat/createConsumer";
        }

        // Make sure ends with .jar
        if (!file.getOriginalFilename().endsWith(".jar")) {
            bindingResult.addError(new FieldError(
                "messageFormatForm", "file", "", true, null, null, "File must have a .jar extension")
            );
            return "/configuration/messageFormat/createConsumer";
        }

        try {
            // Sanitize file name.
            final String filename = messageFormatForm.getName().replaceAll("[^A-Za-z0-9]", "_") + ".jar";

            // Persist jar on filesystem
            final String jarPath = uploadManager.handleDeserializerUpload(file, filename);

            // Attempt to load jar?
            try {
                deserializerLoader.getDeserializer(filename, messageFormatForm.getClasspath());
            } catch (LoaderException e) {
                // Remove jar
                Files.delete(new File(jarPath).toPath());

                bindingResult.addError(new FieldError(
                    "messageFormatForm", "file", "", true, null, null, e.getMessage())
                );
                return "/configuration/messageFormat/createConsumer";
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
     * POST deletes the selected message format
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String deleteCluster(final @PathVariable Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        final MessageFormat messageFormat = messageFormatRepository.findOne(id);
        if (messageFormat == null || messageFormat.isDefaultFormat()) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to remove message format!"));
        } else {
            try {
                // Delete jar from disk
                Files.delete(deserializerLoader.getPathForJar(messageFormat.getJar()));

                // Delete entity
                messageFormatRepository.delete(id);
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Deleted message format!"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // redirect to cluster index
        return "redirect:/configuration/messageFormat";
    }

    private void setupBreadCrumbs(final Model model, String name, String url) {
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
