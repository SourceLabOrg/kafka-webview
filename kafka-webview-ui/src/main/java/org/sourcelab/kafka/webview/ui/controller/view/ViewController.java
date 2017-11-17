/**
 * MIT License
 *
 * Copyright (c) 2017 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.controller.view;

import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.View;
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

/**
 * Controller for consuming/browsing a topic/view.
 */
@Controller
@RequestMapping("/view")
public class ViewController extends BaseController {
    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private ClusterRepository clusterRepository;

    /**
     * GET views index.
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {

        // Setup breadcrumbs
        new BreadCrumbManager(model)
            .addCrumb("View", null);

        // Retrieve all clusters
        final Iterable<Cluster> clusters = clusterRepository.findAllByOrderByNameAsc();
        final Map<Long, Iterable<View>> viewsByClusterId = new HashMap<>();

        for (final Cluster cluster: clusters) {
            // Retrieve all views for cluster
            final Iterable<View> views = viewRepository.findAllByClusterIdOrderByNameAsc(cluster.getId());
            viewsByClusterId.put(cluster.getId(), views);
        }

        // Set model Attributes
        model.addAttribute("viewsByClusterId", viewsByClusterId);
        model.addAttribute("clusters", clusters);

        return "view/index";
    }

    /**
     * GET Displays view for specified view.
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public String index(
        @PathVariable final Long id,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Retrieve the view
        final View view = viewRepository.findOne(id);
        if (view == null) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));

            // redirect to home
            return "redirect:/";
        }

        // Setup breadcrumbs
        new BreadCrumbManager(model)
            .addCrumb("View", "/view")
            .addCrumb(view.getName());

        // Set model Attributes
        model.addAttribute("view", view);
        model.addAttribute("cluster", view.getCluster());

        return "view/consume";
    }
}
