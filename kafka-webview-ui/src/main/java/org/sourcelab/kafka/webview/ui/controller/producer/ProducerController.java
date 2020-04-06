package org.sourcelab.kafka.webview.ui.controller.producer;

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

import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.Producer;
import org.sourcelab.kafka.webview.ui.model.ProducerMessage;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.repository.ProducerMessageRepository;
import org.sourcelab.kafka.webview.ui.repository.ProducerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Producer Operations.
 */
@Controller
@RequestMapping("/producer")
public class ProducerController {
    @Autowired
    private ProducerRepository producerRepository;

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private ProducerMessageRepository producerMessageRepository;

    /**
     * GET producers index.
     */
    @GetMapping
    public String index(
        final Model model,
        @RequestParam( name = "clusterId", required = false) final Long clusterId
    ) {
        // Setup breadcrumbs
        final BreadCrumbManager breadCrumbManager = new BreadCrumbManager(model);

        // Retrieve all clusters and index by id
        final Map<Long, Cluster> clustersById = new HashMap<>();
        clusterRepository
            .findAllByOrderByNameAsc()
            .forEach((cluster) -> clustersById.put(cluster.getId(), cluster));

        final Iterable<Producer> producers;
        if (clusterId == null) {
            // Retrieve all views order by name asc.
            producers = producerRepository.findAllByOrderByNameAsc();
        } else {
            // Retrieve only views for the cluster
            producers = producerRepository.findAllByClusterIdOrderByNameAsc(clusterId);
        }

        // Set model Attributes
        model.addAttribute("producerList", producers);
        model.addAttribute("clustersById", clustersById);

        final String clusterName;
        if (clusterId != null && clustersById.containsKey(clusterId)) {
            // If filtered by a cluster
            clusterName = clustersById.get(clusterId).getName();

            // Add top level breadcrumb
            breadCrumbManager
                .addCrumb("Producer", "/producer")
                .addCrumb("Cluster: " + clusterName);
        } else {
            // If showing all views
            clusterName = null;

            // Add top level breadcrumb
            breadCrumbManager.addCrumb("Producer", null);
        }
        model.addAttribute("clusterName", clusterName);

        return "producer/index";
    }


    /**
     * GET Displays producer for specified id.
     */
    @GetMapping( path = "/{id}")
    public String produce(
        @PathVariable final Long id,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Retrieve the producer
        final Optional<Producer> producerOptional = producerRepository.findById(id);
        if (!producerOptional.isPresent()) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find producer!"));

            // redirect to home
            return "redirect:/";
        }
        final Producer producer = producerOptional.get();

        final Optional<ProducerMessage> producerMessageOptional = producerMessageRepository.findByProducer( producer );
        if (!producerMessageOptional.isPresent()) {
            // yeah, I don't know. This shouldn't be possible
            return "redirect:/";
        }
        final ProducerMessage producerMessage = producerMessageOptional.get();

        // Setup breadcrumbs
        new BreadCrumbManager(model)
            .addCrumb("Producer", "/producer")
            .addCrumb(producer.getName());

        // Set model Attributes
        model.addAttribute("producer", producer);
        model.addAttribute("cluster", producer.getCluster());
        model.addAttribute( "producerMessage", producerMessage );

        return "producer/produce";
    }
}
