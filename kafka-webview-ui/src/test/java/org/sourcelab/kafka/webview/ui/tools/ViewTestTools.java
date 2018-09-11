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

package org.sourcelab.kafka.webview.ui.tools;

import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * Helpful tools for Views in tests.
 */
@Component
public class ViewTestTools {
    private final ViewRepository viewRepository;
    private final ClusterTestTools clusterTestTools;
    private final MessageFormatTestTools messageFormatTestTools;

    @Autowired
    public ViewTestTools(
        final ViewRepository viewRepository,
        final ClusterTestTools clusterTestTools,
        final MessageFormatTestTools messageFormatTestTools) {
        this.viewRepository = viewRepository;
        this.clusterTestTools = clusterTestTools;
        this.messageFormatTestTools = messageFormatTestTools;
    }

    public View createView(final String name) {
        // Create a dummy cluster
        final Cluster cluster = clusterTestTools.createCluster(name);

        // Create a dummy message format
        final MessageFormat messageFormat = messageFormatTestTools.createMessageFormat(name);

        // Create it.
        return createView(name, cluster, messageFormat);
    }

    public View createViewWithFormat(final String name, final MessageFormat messageFormat) {
        // Create a dummy cluster
        final Cluster cluster = clusterTestTools.createCluster(name);

        // Create it.
        return createView(name, cluster, messageFormat);
    }

    public View createView(
        final String name,
        final Cluster cluster,
        final MessageFormat messageFormat) {
        final View view = new View();
        view.setName(name);
        view.setCluster(cluster);
        view.setKeyMessageFormat(messageFormat);
        view.setValueMessageFormat(messageFormat);
        view.setPartitions("");
        view.setTopic("MyTopic");
        view.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        view.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        viewRepository.save(view);
        return view;
    }

    /**
     * Easy access to viewRepository.
     * @param view View to persist.
     */
    public void save(final View view) {
        viewRepository.save(view);
    }

}
