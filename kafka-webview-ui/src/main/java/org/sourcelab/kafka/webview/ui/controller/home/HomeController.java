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

package org.sourcelab.kafka.webview.ui.controller.home;

import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Home controller actions.
 */
@Controller
public class HomeController extends BaseController {

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private ViewRepository viewRepository;

    /**
     * Once things are setup, we'll redirect the home page to view/index.
     */
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(final Model model) {
        // Look for views
        final long numberOfViews = viewRepository.count();

        // If at least one view is configured
        if (numberOfViews > 0) {
            // Redirect to the vew index
            return "redirect:/view";
        }

        // Setup breadcrumbs
        new BreadCrumbManager(model);

        // Look for clusters
        final long numberOfClusters = clusterRepository.count();

        model.addAttribute("hasView", (numberOfViews > 0));
        model.addAttribute("hasCluster", (numberOfClusters > 0));
        return "home/index";
    }

    /**
     * Provides in App Help documentation.
     */
    @RequestMapping(path = "/help", method = RequestMethod.GET)
    public String help(final Model model) {
        // Setup breadcrumbs
        new BreadCrumbManager(model)
            .addCrumb("Help");

        return "home/help";
    }
}
