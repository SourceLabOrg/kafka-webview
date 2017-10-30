package com.darksci.kafka.webview.ui.controller.configuration.filter;

import com.darksci.kafka.webview.ui.controller.BaseController;
import com.darksci.kafka.webview.ui.controller.configuration.filter.forms.FilterForm;
import com.darksci.kafka.webview.ui.manager.plugin.PluginFactory;
import com.darksci.kafka.webview.ui.manager.plugin.UploadManager;
import com.darksci.kafka.webview.ui.manager.plugin.exception.LoaderException;
import com.darksci.kafka.webview.ui.manager.ui.BreadCrumbManager;
import com.darksci.kafka.webview.ui.manager.ui.FlashMessage;
import com.darksci.kafka.webview.ui.model.Filter;
import com.darksci.kafka.webview.ui.model.ViewToFilterOptional;
import com.darksci.kafka.webview.ui.repository.ViewToFilterEnforcedRepository;
import com.darksci.kafka.webview.ui.repository.ViewToFilterOptionalRepository;
import com.darksci.kafka.webview.ui.model.ViewToFilterEnforced;
import com.darksci.kafka.webview.ui.plugin.filter.RecordFilter;
import com.darksci.kafka.webview.ui.repository.FilterRepository;
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
import java.util.List;

@Controller
@RequestMapping("/configuration/filter")
public class FilterController extends BaseController {

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private PluginFactory<RecordFilter> recordFilterPluginFactory;

    @Autowired
    private FilterRepository filterRepository;

    @Autowired
    private ViewToFilterEnforcedRepository viewToFilterEnforcedRepository;

    @Autowired
    private ViewToFilterOptionalRepository viewToFilterOptionalRepository;

    /**
     * GET Displays main filters index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all message formats
        final Iterable<Filter> filterList = filterRepository.findAllByOrderByNameAsc();
        model.addAttribute("filters", filterList);

        return "configuration/filter/index";
    }

    /**
     * GET Displays createWebClient filter form.
     */
    @RequestMapping(path = "/createWebClient", method = RequestMethod.GET)
    public String createFilter(final FilterForm filterForm, final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", null);

        return "configuration/filter/createWebClient";
    }

    @RequestMapping(path = "/createWebClient", method = RequestMethod.POST)
    public String create(
        @Valid final FilterForm filterForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes) {

        // If we have errors just display the form again.
        if (bindingResult.hasErrors()) {
            return "configuration/filter/createWebClient";
        }

        final MultipartFile file = filterForm.getFile();
        if (file.isEmpty()) {
            bindingResult.addError(new FieldError(
                "filterForm", "file", "", true, null, null, "Select a jar to upload")
            );
            return "/configuration/filter/createWebClient";
        }

        // Make sure ends with .jar
        if (!file.getOriginalFilename().endsWith(".jar")) {
            bindingResult.addError(new FieldError(
                "filterForm", "file", "", true, null, null, "File must have a .jar extension")
            );
            return "/configuration/filter/createWebClient";
        }

        try {
            // Sanitize filename
            final String filename = filterForm.getName().replaceAll("[^A-Za-z0-9]", "_") + ".jar";

            // Persist jar on filesystem
            final String jarPath = uploadManager.handleFilterUpload(file, filename);

            // Attempt to load jar?
            try {
                recordFilterPluginFactory.getPlugin(filename, filterForm.getClasspath());
            } catch (LoaderException e) {
                // Remove jar
                Files.delete(new File(jarPath).toPath());

                bindingResult.addError(new FieldError(
                    "filterForm", "file", "", true, null, null, e.getMessage())
                );
                return "/configuration/filter/createWebClient";
            }

            final Filter filter = new Filter();
            filter.setName(filterForm.getName());
            filter.setClasspath(filterForm.getClasspath());
            filter.setJar(filename);
            filterRepository.save(filter);
        } catch (IOException e) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to save uploaded JAR: " + e.getMessage()));

            // redirect to filter index
            return "redirect:/configuration/filter";
        }

        redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Successfully created filter!"));
        return "redirect:/configuration/filter";
    }

    /**
     * POST deletes the selected filter
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String delete(final @PathVariable Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        final Filter filter = filterRepository.findOne(id);
        if (filter == null) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to remove filter!"));
        } else {
            try {
                // Delete any children
                final List<ViewToFilterEnforced> enforcedList = viewToFilterEnforcedRepository.findByFilterId(id);
                final List<ViewToFilterOptional> optionalList = viewToFilterOptionalRepository.findByFilterId(id);

                viewToFilterEnforcedRepository.delete(enforcedList);
                viewToFilterOptionalRepository.delete(optionalList);

                // Delete entity
                filterRepository.delete(id);

                // Delete jar from disk
                Files.delete(recordFilterPluginFactory.getPathForJar(filter.getJar()));
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Deleted filter!"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // redirect to cluster index
        return "redirect:/configuration/filter";
    }

    private void setupBreadCrumbs(final Model model, String name, String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
            .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb("Filters", "/configuration/filter");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Filters", null);
        }
    }
}
