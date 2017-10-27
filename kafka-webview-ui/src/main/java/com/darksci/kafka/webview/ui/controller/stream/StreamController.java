package com.darksci.kafka.webview.ui.controller.stream;

import com.darksci.kafka.webview.ui.controller.BaseController;
import com.darksci.kafka.webview.ui.manager.kafka.KafkaConsumerFactory;
import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumer;
import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import com.darksci.kafka.webview.ui.model.View;
import com.darksci.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;

/**
 * Websocket controller end points.
 */
@Controller
public class StreamController extends BaseController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private WebKafkaConsumerFactory consumerFactory;


    @MessageMapping("/subscribe/{viewId}")
    @SendTo("/topic/greetings")
    public String subscribe(
        final @DestinationVariable Long viewId,
        final Authentication auth) {

        // Retrieve view
        final View view = viewRepository.findOne(viewId);
        if (view == null) {
            throw new RuntimeException("TODO Better handling");
        }

        // Create consumer
        final WebKafkaConsumer consumer = consumerFactory.create(view, new ArrayList<>(), getLoggedInUserId());


        //messagingTemplate.convertAndSendToUser(user, topic, Notification.newFightDetails(fightDetails));
        return "";
    }
}
