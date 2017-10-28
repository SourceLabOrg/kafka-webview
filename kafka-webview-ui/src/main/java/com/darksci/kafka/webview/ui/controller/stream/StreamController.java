package com.darksci.kafka.webview.ui.controller.stream;

import com.darksci.kafka.webview.ui.controller.BaseController;
import com.darksci.kafka.webview.ui.manager.kafka.KafkaConsumerFactory;
import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumer;
import com.darksci.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import com.darksci.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
import com.darksci.kafka.webview.ui.model.View;
import com.darksci.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;

/**
 * Websocket controller end points.
 */
@Controller
public class StreamController extends BaseController {
    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private WebKafkaConsumerFactory consumerFactory;

    @Autowired
    private WebSocketConsumersManager webSocketConsumersManager;


    @MessageMapping("/consume/{viewId}")
    @SendTo("/topic/notifications")
    public String consume(
        final @DestinationVariable Long viewId) {

        // Retrieve view
        final View view = viewRepository.findOne(viewId);
        if (view == null) {
            throw new RuntimeException("TODO Better handling");
        }

        // Subscribe
        webSocketConsumersManager.addNewConsumer(view, getLoggedInUserId());
        return "User " + getLoggedInUser().getUsername() + " Subscribed to View " + view.getName();
    }

    @RequestMapping(path = "/stream", method = RequestMethod.GET)
    public String streamIndex() {
        return "stream/index";
    }
}
