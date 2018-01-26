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

package org.sourcelab.kafka.webview.ui.controller.configuration.filter;

import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.configuration.filter.forms.FilterForm;
import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.UploadManager;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.LoaderException;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterEnforced;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterOptional;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;
import org.sourcelab.kafka.webview.ui.repository.FilterRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewToFilterEnforcedRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewToFilterOptionalRepository;
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
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for CRUD Operations on Filters.
 */
@Controller
@RequestMapping("/configuration/filter")
public class FilterConfigController extends BaseController {

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
     * GET Displays create filter form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createFilter(final FilterForm filterForm, final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, "Create", null);

        return "configuration/filter/create";
    }

    /**
     * GET Displays edit filter form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editFilter(
        @PathVariable final Long id,
        final FilterForm filterForm,
        final Model model,
        final RedirectAttributes redirectAttributes) {
        // Retrieve it
        final Filter filter = filterRepository.findOne(id);
        if (filter == null) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find filter!"));
            return "redirect:/configuration/filter";
        }

        // Setup breadcrumbs
        setupBreadCrumbs(model, "Edit " + filter.getName(), null);

        // Setup form
        filterForm.setId(filter.getId());
        filterForm.setName(filter.getName());
        filterForm.setClasspath(filter.getClasspath());

        return "configuration/filter/create";
    }

    /**
     * POST Create or Update a filter.
     */
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public String update(
        @Valid final FilterForm filterForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes) {

        // If we have errors just display the form again.
        if (bindingResult.hasErrors()) {
            return "configuration/filter/create";
        }

        // Grab uploaded file
        final MultipartFile file = filterForm.getFile();

        // If the filter doesnt exist, and no file uploaded.
        if (!filterForm.exists() && file.isEmpty()) {
            // That's an error.
            bindingResult.addError(new FieldError(
                "filterForm", "file", "", true, null, null, "Select a jar to upload")
            );
            return "configuration/filter/create";
        }

        // If filter exists
        final Filter filter;
        if (filterForm.exists()) {
            // Retrieve Filter
            filter = filterRepository.findOne(filterForm.getId());

            // If we can't find the filter
            if (filter == null) {
                // Set flash message & redirect
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find filter!"));
                return "redirect:/configuration/filter";
            }
        } else {
            // Creating new filter
            filter = new Filter();
        }

        // Ensure that filter's name is unique
        final Filter existingFilter = filterRepository.findByName(filterForm.getName());
        if (existingFilter != null) {
            if (!filterForm.exists() || existingFilter.getId() != filterForm.getId()) {
                // Name is in use!
                bindingResult.addError(new FieldError(
                    "filterForm",
                    "name",
                    filterForm.getName(),
                    true,
                    null,
                    null,
                    "Filter must have unique name")
                );
                return "configuration/filter/create";
            }
        }

        // Set properties.
        filter.setName(filterForm.getName());

        // If they uploaded a file.
        if (!file.isEmpty()) {
            // Make sure ends with .jar
            if (!file.getOriginalFilename().endsWith(".jar")) {
                bindingResult.addError(new FieldError(
                    "filterForm", "file", "", true, null, null, "File must have a .jar extension")
                );
                return "configuration/filter/create";
            }

            try {
                // Sanitize filename
                final String filename = filterForm.getName().replaceAll("[^A-Za-z0-9]", "_") + ".jar";
                final String tmpFilename = filename + ".tmp";

                // Persist jar on filesystem into temp location
                final String tmpJarLocation = uploadManager.handleFilterUpload(file, tmpFilename);
                final String finalJarLocation = tmpJarLocation.substring(0, tmpJarLocation.lastIndexOf(".tmp"));

                // Attempt to load jar?
                final String filterOptionNames;
                try {
                    final RecordFilter recordFilter = recordFilterPluginFactory.getPlugin(tmpFilename, filterForm.getClasspath());
                    final Set<String> filterOptions = recordFilter.getOptionNames();

                    // Makes assumption strings contain no commas!
                    filterOptionNames = filterOptions.stream().collect(Collectors.joining(","));
                } catch (final LoaderException exception) {
                    // Remove jar
                    Files.delete(new File(tmpJarLocation).toPath());

                    bindingResult.addError(new FieldError(
                        "filterForm", "file", "", true, null, null, exception.getMessage())
                    );
                    return "configuration/filter/create";
                }

                // If successful overwrite original jar
                final Path tmpJarPath = new File(tmpJarLocation).toPath();
                final Path finalJarPath = new File(finalJarLocation).toPath();
                Files.deleteIfExists(finalJarPath);
                Files.move(tmpJarPath, finalJarPath);

                // Set properties
                filter.setClasspath(filterForm.getClasspath());
                filter.setJar(filename);
                filter.setOptions(filterOptionNames);
            } catch (IOException e) {
                // Set flash message
                redirectAttributes
                    .addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to save uploaded JAR: " + e.getMessage()));

                // redirect to filter index
                return "redirect:/configuration/filter";
            }
        }

        // Save entity
        filterRepository.save(filter);

        redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Successfully created filter!"));
        return "redirect:/configuration/filter";
    }

    /**
     * POST deletes the selected filter.
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String delete(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
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

    private void setupBreadCrumbs(final Model model, final String name, final String url) {
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
