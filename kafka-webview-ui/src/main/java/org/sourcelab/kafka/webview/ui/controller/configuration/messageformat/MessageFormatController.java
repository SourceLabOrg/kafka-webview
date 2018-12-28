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

package org.sourcelab.kafka.webview.ui.controller.configuration.messageformat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.configuration.messageformat.forms.MessageFormatForm;
import org.sourcelab.kafka.webview.ui.manager.controller.EntityUsageManager;
import org.sourcelab.kafka.webview.ui.manager.controller.UploadableJarControllerHelper;
import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.UploadManager;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.LoaderException;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.PartitioningStrategy;
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
import java.util.Collections;
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
        return getHelper().buildIndex(model);
    }

    /**
     * GET Displays create message format form.
     */
    @RequestMapping(path = "/create", method = RequestMethod.GET)
    public String createMessageFormat(final MessageFormatForm form, final Model model) {
        return getHelper().buildCreate(model);
    }

    /**
     * GET Displays edit message format form.
     */
    @RequestMapping(path = "/edit/{id}", method = RequestMethod.GET)
    public String editMessageFormat(
        @PathVariable final Long id,
        final MessageFormatForm form,
        final Model model,
        final RedirectAttributes redirectAttributes) {

        return getHelper().buildEdit(id, form, model, redirectAttributes);
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
        @Valid final MessageFormatForm form,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        @RequestParam final Map<String, String> allRequestParams) {

        return getHelper()
            .handleUpdate(form, bindingResult, redirectAttributes);
    }

    /**
     * POST deletes the selected message format.
     */
    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String deleteCluster(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {

        return getHelper()
            .processDelete(id, redirectAttributes, entityId -> {
                final Iterable<View> views = viewRepository.findAllByKeyMessageFormatIdOrValueMessageFormatIdOrderByNameAsc(entityId, entityId);

                final Collection<String> viewNames = new ArrayList<>();
                for (final View view: views) {
                    viewNames.add(view.getName());
                }

                if (viewNames.isEmpty()) {
                    return Collections.emptyMap();
                }
                return Collections.singletonMap("views", viewNames);
            });
    }

    private UploadableJarControllerHelper<MessageFormat> getHelper() {
        return new UploadableJarControllerHelper<>(
            "Message Format",
            "Message Formats",
            "configuration/messageFormat",
            MessageFormat.class,
            uploadManager,
            deserializerLoader,
            messageFormatRepository
        );
    }
}
