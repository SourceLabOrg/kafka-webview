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

}
