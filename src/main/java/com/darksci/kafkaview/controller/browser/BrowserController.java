package com.darksci.kafkaview.controller.browser;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.manager.kafka.KafkaConsumerFactory;
import com.darksci.kafkaview.manager.kafka.TransactionalKafkaClient;
import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.config.DeserializerConfig;
import com.darksci.kafkaview.manager.kafka.config.FilterConfig;
import com.darksci.kafkaview.manager.kafka.config.TopicConfig;
import com.darksci.kafkaview.manager.kafka.dto.KafkaResults;
import com.darksci.kafkaview.manager.plugin.DeserializerLoader;
import com.darksci.kafkaview.manager.plugin.exception.LoaderException;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.model.MessageFormat;
import com.darksci.kafkaview.model.View;
import com.darksci.kafkaview.repository.ViewRepository;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/browser")
public class BrowserController extends BaseController {
    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private DeserializerLoader deserializerLoader;

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "/{id}/{direction}", method = RequestMethod.GET)
    public String index(
        final @PathVariable Long id,
        final @PathVariable String direction,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        // Retrieve the browser
        final View view = viewRepository.findOne(id);
        if (view == null) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find browser!"));

            // redirect to home
            return "redirect:/";
        }
        model.addAttribute("view", view);

        // Create consumer
        final KafkaResults results;
        try (final TransactionalKafkaClient transactionalKafkaClient = setup(view)) {
            // move directions if needed
            if ("n".equals(direction)) {
                transactionalKafkaClient.next();
            } else if ("p".equals(direction)) {
                transactionalKafkaClient.previous();
            } else if ("r".equals(direction)) {
                transactionalKafkaClient.toHead();
            }

            // Poll
            results = transactionalKafkaClient.consumePerPartition();
        }
        model.addAttribute("results", results.getResults());
        return "browser/index";
    }

    private TransactionalKafkaClient setup(final View view) {
        // Construct a consumerId based on user
        final String consumerId = "MyUserId1";

        // Grab our relevant bits
        final Cluster cluster = view.getCluster();
        final MessageFormat keyMessageFormat = view.getKeyMessageFormat();
        final MessageFormat valueMessageFormat = view.getValueMessageFormat();

        final Class keyDeserializerClass;
        try {
            if (keyMessageFormat.isDefaultFormat()) {
                keyDeserializerClass = deserializerLoader.getDeserializerClass(keyMessageFormat.getClasspath());
            } else {
                keyDeserializerClass = deserializerLoader.getDeserializerClass(keyMessageFormat.getJar(), keyMessageFormat.getClasspath());
            }
        } catch (final LoaderException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }

        final Class valueDeserializerClass;
        try {
            if (valueMessageFormat.isDefaultFormat()) {
                valueDeserializerClass = deserializerLoader.getDeserializerClass(valueMessageFormat.getClasspath());
            } else {
                valueDeserializerClass = deserializerLoader.getDeserializerClass(valueMessageFormat.getJar(), valueMessageFormat.getClasspath());
            }
        } catch (final LoaderException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }

        final ClusterConfig clusterConfig = new ClusterConfig(cluster.getBrokerHosts());
        final DeserializerConfig deserializerConfig = new DeserializerConfig(keyDeserializerClass, valueDeserializerClass);
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, view.getTopic());
        final ClientConfig clientConfig = new ClientConfig(topicConfig, FilterConfig.withNoFilters(), consumerId);

        // Create the damn consumer
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory(clientConfig);
        final KafkaConsumer kafkaConsumer = kafkaConsumerFactory.createAndSubscribe();

        // Create consumer
        final KafkaResults results;
        return new TransactionalKafkaClient(kafkaConsumer, clientConfig);
    }
}
