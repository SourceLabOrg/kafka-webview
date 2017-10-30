package com.darksci.kafka.webview.ui.controller.stream;

import com.darksci.kafka.webview.ui.controller.BaseController;
import com.darksci.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
import com.darksci.kafka.webview.ui.manager.user.CustomUserDetails;
import com.darksci.kafka.webview.ui.model.View;
import com.darksci.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.transaction.Transactional;

/**
 * Websocket controller end points.
 */
@Controller
public class StreamController extends BaseController {
    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private WebSocketConsumersManager webSocketConsumersManager;

    /**
     * Serves standard http requested page with client JS code.
     */
    @RequestMapping(path = "/stream", method = RequestMethod.GET)
    public String streamIndex(final Model model) {
        // Fixed for now
        model.addAttribute("viewId", 1L);
        model.addAttribute("userId", getLoggedInUserId());

        return "stream/index";
    }

    /**
     * Serves websocket requests, requesting to start a stream on the given view.
     */
    @MessageMapping("/consume/{viewId}")
    @Transactional
    public String newConsumer(
        final @DestinationVariable Long viewId,
        final Authentication auth) {

        // Retrieve view
        final View view = viewRepository.findOne(viewId);
        if (view == null) {
            throw new RuntimeException("TODO Better handling");
        }

        // Subscribe
        final long userId = getLoggedInUserId(auth);
        final String username = auth.getName();
        webSocketConsumersManager.addNewConsumer(view, userId, username);
        return "{success: true}";
    }

    /**
     * @return Currently logged in user Id.
     */
    private long getLoggedInUserId(final Authentication authentication) {
        return getLoggedInUser(authentication).getUserId();
    }

    /**
     * @return Currently logged in user's details.
     */
    private CustomUserDetails getLoggedInUser(final Authentication authentication) {
        return ((CustomUserDetails) authentication.getPrincipal());
    }
}
