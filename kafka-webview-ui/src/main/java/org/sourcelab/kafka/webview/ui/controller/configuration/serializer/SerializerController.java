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

package org.sourcelab.kafka.webview.ui.controller.configuration.serializer;

import org.apache.kafka.common.serialization.Serializer;
import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.configuration.serializer.forms.SerializerForm;
import org.sourcelab.kafka.webview.ui.manager.controller.UploadableJarControllerHelper;
import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.UploadManager;
import org.sourcelab.kafka.webview.ui.model.SerializerFormat;
import org.sourcelab.kafka.webview.ui.plugin.serializer.SerializerTransformer;
import org.sourcelab.kafka.webview.ui.repository.SerializerFormatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

/**
 * Controller for Serializer CRUD operations.
 */
@Controller
@RequestMapping("/configuration/serializer")
public class SerializerController extends BaseController {

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private PluginFactory<SerializerTransformer> serializerPluginFactory;

    @Autowired
    private SerializerFormatRepository serializerFormatRepository;

    /**
     * GET Displays main message format index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        return getHelper().buildIndex(model);
    }

    /**
     * GET Displays create serializer format form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String create(final SerializerForm form, final Model model) {
        return getHelper().buildCreate(model);
    }

    /**
     * GET Displays edit serializer form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String edit(
        @PathVariable final Long id,
        final SerializerForm form,
        final Model model,
        final RedirectAttributes redirectAttributes) {

        return getHelper()
            .buildEdit(id, form, model, redirectAttributes);
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
    public String update(
        @Valid final SerializerForm form,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        @RequestParam final Map<String, String> allRequestParams
    ) {
        return getHelper().handleUpdate(form, bindingResult, redirectAttributes);
    }

    /**
     * POST deletes the selected partitioning strategy.
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String delete(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        return getHelper()
            .processDelete(id, redirectAttributes, entityId -> Collections.emptyList());
    }

    private UploadableJarControllerHelper<SerializerFormat> getHelper() {
        return new UploadableJarControllerHelper<>(
            "Serialization Format",
            "Serialization Formats",
            "configuration/serializer",
            SerializerFormat.class,
            uploadManager,
            serializerPluginFactory,
            serializerFormatRepository
        );
    }
}
