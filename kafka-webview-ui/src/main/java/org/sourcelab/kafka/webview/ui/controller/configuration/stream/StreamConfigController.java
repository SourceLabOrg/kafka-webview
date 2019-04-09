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

package org.sourcelab.kafka.webview.ui.controller.configuration.stream;

import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.manager.socket.StreamConsumerDetails;
import org.sourcelab.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.manager.user.permission.RequirePermission;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.User;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.repository.UserRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for Stream configuration.
 */
@Controller
@RequestMapping("/configuration/stream")
public class StreamConfigController extends BaseController {

    private final ClusterRepository clusterRepository;
    private final ViewRepository viewRepository;
    private final UserRepository userRepository;
    private final WebSocketConsumersManager webSocketConsumersManager;

    /**
     * Constructor.
     * @param clusterRepository repository instance.
     * @param viewRepository repository instance.
     * @param userRepository repository instance.
     * @param webSocketConsumersManager instance.
     */
    @Autowired
    public StreamConfigController(
        final ClusterRepository clusterRepository,
        final ViewRepository viewRepository,
        final UserRepository userRepository,
        final WebSocketConsumersManager webSocketConsumersManager
    ) {
        this.clusterRepository = clusterRepository;
        this.viewRepository = viewRepository;
        this.userRepository = userRepository;
        this.webSocketConsumersManager = webSocketConsumersManager;
    }

    /**
     * GET Displays currently active socket consumers.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    @RequirePermission(Permissions.CONSUMER_READ)
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all consumers
        final Collection<StreamConsumerDetails> consumers = webSocketConsumersManager.getConsumers();

        // Loop thru and collect views and users
        final Set<Long> userIds = new HashSet<>();
        final Set<Long> viewIds = new HashSet<>();

        consumers.forEach((consumer) -> {
            userIds.add(consumer.getUserId());
            viewIds.add(consumer.getViewId());

        });

        // Map by Id
        final Map<Long, User> userMap = new HashMap<>();
        final Map<Long, View> viewMap = new HashMap<>();
        final Map<Long, Cluster> clusterMap = new HashMap<>();

        // Retrieve users and views
        userRepository.findAllById(userIds).forEach((user) -> userMap.put(user.getId(), user));
        viewRepository.findAllById(viewIds).forEach((view) -> viewMap.put(view.getId(), view));

        // Build Cluster Map
        final Set<Long> clusterIds = viewMap.values()
            .stream()
            .map((view) -> view.getCluster().getId())
            .collect(Collectors.toSet());
        clusterRepository.findAllById(clusterIds).forEach((cluster) -> clusterMap.put(cluster.getId(), cluster));

        // Add to UI model
        model.addAttribute("viewMap", viewMap);
        model.addAttribute("userMap", userMap);
        model.addAttribute("clusterMap", clusterMap);
        model.addAttribute("consumers", consumers);

        return "configuration/stream/index";
    }

    /**
     * POST deletes the selected cluster.
     */
    @RequestMapping(path = "/close/{hash}", method = RequestMethod.POST)
    @RequirePermission(Permissions.CONSUMER_DELETE)
    public String closeConsumer(@PathVariable final String hash, final RedirectAttributes redirectAttributes) {
        // Close by hash
        if (webSocketConsumersManager.removeConsumersForSessionHash(hash)) {
            // Notify requesting user.
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Closed consumer!"));
        } else {
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find consumer!"));
        }

        // redirect to index
        return "redirect:/configuration/stream";
    }

    private void setupBreadCrumbs(final Model model, final String name, final String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
            .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb("Streams", "/configuration/stream");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Streams", null);
        }
    }
}