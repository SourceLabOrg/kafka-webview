package com.darksci.kafkaview.controller.view;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.manager.kafka.KafkaConsumerFactory;
import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.config.DeserializerConfig;
import com.darksci.kafkaview.manager.kafka.config.TopicConfig;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.model.MessageFormat;
import com.darksci.kafkaview.model.View;
import com.darksci.kafkaview.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//@Controller
//@RequestMapping("/view")
public class ViewController extends BaseController {
    @Autowired
    private ViewRepository viewRepository;

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "/browser/{id}", method = RequestMethod.GET)
    public String index(
        final @PathVariable Long id,
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

        // Construct a consumerId based on user
        final String consumerId = "MyUserId";

        // Grab our relevant bits
        final Cluster cluster = view.getCluster();
        //final MessageFormat messageFormat = view.getMessageFormat();

//        final ClusterConfig clusterConfig = new ClusterConfig(cluster.getBrokerHosts());
//        final DeserializerConfig deserializerConfig = new DeserializerConfig(messageFormat.)
//        final TopicConfig topicConfig = new TopicConfig(clusterConfig, )
//        final ClientConfig clientConfig = new ClientConfig(cluster.getBrokerHosts(), consumerId);

        return "configuration/view/index";
    }
}
