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

package org.sourcelab.kafka.webview.ui.controller.cluster;

import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for viewing Cluster details.
 */
@Controller
@RequestMapping("/cluster")
public class ClusterController extends BaseController {

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private ViewRepository viewRepository;

    /**
     * GET Displays cluster list.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String clusterIndex(final Model model, final RedirectAttributes redirectAttributes) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model);
        manager.addCrumb("Cluster Explorer", null);

        // Retrieve all clusters
        final Iterable<Cluster> clusterList = clusterRepository.findAll();
        model.addAttribute("clusterList", clusterList);

        // Retrieve how many views for each cluster
        final Map<Long, Long> viewsByClusterId = new HashMap<>();
        for (final Cluster cluster: clusterList) {
            final Long clusterId = cluster.getId();
            final Long count = viewRepository.countByClusterId(cluster.getId());
            viewsByClusterId.put(clusterId, count);
        }
        model.addAttribute("viewsByClusterId", viewsByClusterId);

        // Display template
        return "cluster/index";
    }

    /**
     * GET Displays edit cluster form.
     */
    @RequestMapping(path = "/{clusterId}", method = RequestMethod.GET)
    public String readCluster(
        @PathVariable final Long clusterId,
        final Model model,
        final RedirectAttributes redirectAttributes) {

        // Retrieve by id
        final Cluster cluster = retrieveCluster(clusterId, redirectAttributes);
        if (cluster == null) {
            // redirect
            return "redirect:/";
        }
        model.addAttribute("cluster", cluster);

        // Setup breadcrumbs
        setupBreadCrumbs(model)
            .addCrumb(cluster.getName(), null);

        // Display template
        return "cluster/read";
    }

    /**
     * GET Displays info about a specific broker in a cluster.
     */
    @RequestMapping(path = "/{clusterId}/broker/{brokerId}", method = RequestMethod.GET)
    public String readBroker(
        @PathVariable final Long clusterId,
        @PathVariable final Integer brokerId,
        final Model model,
        final RedirectAttributes redirectAttributes) {

        // Retrieve by id
        final Cluster cluster = retrieveCluster(clusterId, redirectAttributes);
        if (cluster == null) {
            // redirect
            return "redirect:/";
        }
        model.addAttribute("cluster", cluster);
        model.addAttribute("brokerId", brokerId);

        // Setup breadcrumbs
        setupBreadCrumbs(model)
            .addCrumb(cluster.getName(), "/cluster/" + clusterId)
            .addCrumb("Broker " + brokerId, null);

        // Display template
        return "cluster/readBroker";
    }

    /**
     * GET Displays info about a specific topic in a cluster.
     */
    @RequestMapping(path = "/{clusterId}/topic/{topic:.+}", method = RequestMethod.GET)
    public String readTopic(
        @PathVariable final Long clusterId,
        @PathVariable final String topic,
        final Model model,
        final RedirectAttributes redirectAttributes) {

        // Retrieve by id
        final Cluster cluster = retrieveCluster(clusterId, redirectAttributes);
        if (cluster == null) {
            // redirect
            return "redirect:/";
        }
        model.addAttribute("cluster", cluster);
        model.addAttribute("topic", topic);

        // Setup breadcrumbs
        setupBreadCrumbs(model)
            .addCrumb(cluster.getName(), "/cluster/" + clusterId)
            .addCrumb("Topic " + topic, null);


        // Display template
        return "cluster/readTopic";
    }

    private Cluster retrieveCluster(final Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve by id
        final Optional<Cluster> clusterOptional = clusterRepository.findById(id);
        if (!clusterOptional.isPresent()) {
            // redirect
            // Set flash message
            final FlashMessage flashMessage = FlashMessage.newWarning("Unable to find cluster!");
            redirectAttributes.addFlashAttribute("FlashMessage", flashMessage);

            // redirect to cluster index
            return null;
        }
        return clusterOptional.get();
    }

    private BreadCrumbManager setupBreadCrumbs(final Model model) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model);
        manager.addCrumb("Cluster Explorer", "/cluster");
        return manager;
    }
}
