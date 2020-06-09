package org.sourcelab.kafka.webview.ui.controller.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 *
 */
//@RestController
//@RequestMapping("/view")
public class ViewRestController {
    private static final Logger logger = LoggerFactory.getLogger(ViewRestController.class);

    private final ViewRepository viewRepository;

    @Autowired
    public ViewRestController(final ViewRepository viewRepository) {
        this.viewRepository = Objects.requireNonNull(viewRepository);
    }

    //@RequestMapping(path = "/datatable", method = RequestMethod.GET)
    public Page<View> list(final Pageable pageable) {
//        if (false) {
//            final Cluster cluster = clusterRepository.findByName("Dev Cluster");
//            final Iterable<MessageFormat> messageFormats = messageFormatRepository.findAllByOrderByNameAsc();
//            final MessageFormat messageFormat = messageFormats.iterator().next();
//
//            for (int x = 0; x < 100; x++) {
//                View view = new View();
//                view.setName("View #" + x);
//                view.setCluster(cluster);
//                view.setKeyMessageFormat(messageFormat);
//                view.setValueMessageFormat(messageFormat);
//                view.setPartitions("");
//                view.setTopic("MyTopic-" + x);
//                view.setCreatedAt(new Timestamp(System.currentTimeMillis()));
//                view.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
//                viewRepository.save(view);
//            }
//        }

        final Page<View> pageOfViews = viewRepository.findAll(pageable);
        pageOfViews.getContent().forEach((p) -> p.getCluster());
        return pageOfViews;
    }
}
