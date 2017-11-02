package com.darksci.kafka.webview.ui.controller.stream;

import com.darksci.kafka.webview.ui.controller.BaseController;
import com.darksci.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
import com.darksci.kafka.webview.ui.manager.ui.BreadCrumbManager;
import com.darksci.kafka.webview.ui.manager.ui.FlashMessage;
import com.darksci.kafka.webview.ui.manager.user.CustomUserDetails;
import com.darksci.kafka.webview.ui.model.View;
import com.darksci.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;

/**
 * Websocket controller end points.
 */
@Controller
@RequestMapping("/stream")
public class StreamController extends BaseController {
    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private WebSocketConsumersManager webSocketConsumersManager;


    /**
     * Just redirects to view index for now?
     */
    @RequestMapping(path = "", method = RequestMethod.GET)
    public String index(final Model model) {
        // Setup breadcrumbs
        new BreadCrumbManager(model)
            .addCrumb("Stream", null);

        return "redirect:/view";
    }

    /**
     * Serves standard http requested page with client JS code.
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public String stream(
        final @PathVariable Long id,
        final Model model,
        final RedirectAttributes redirectAttributes) {
        // Retrieve view
        final View view = viewRepository.findOne(id);
        if (view == null) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));

            // redirect to home
            return "redirect:/";
        }

        // Setup breadcrumbs
        new BreadCrumbManager(model)
            .addCrumb("Stream", "/stream")
            .addCrumb(view.getName());

        // Set view attributes
        model.addAttribute("view", view);
        model.addAttribute("userId", getLoggedInUserId());

        return "stream/index";
    }

// Here down serves websocket requests

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
            return "{success: false}";
        }

        // Subscribe
        final long userId = getLoggedInUserId(auth);
        final String username = auth.getName();
        webSocketConsumersManager.addNewConsumer(view, userId, username);
        return "{success: true}";
    }

    /**
     * Serves web socket requests, requesting to pause a consumer.
     */
    @MessageMapping("/pause/{viewId}")
    @Transactional
    public String pauseConsumer(
        final @DestinationVariable Long viewId,
        final Authentication auth) {

        // Request a pause
        final long userId = getLoggedInUserId(auth);
        webSocketConsumersManager.pauseConsumer(viewId, userId);
        return "{success: true}";
    }

    /**
     * Serves web socket requests, requesting to resume a consumer.
     */
    @MessageMapping("/resume/{viewId}")
    @Transactional
    public String resumeConsumer(
        final @DestinationVariable Long viewId,
        final Authentication auth) {

        // Request Resume
        final long userId = getLoggedInUserId(auth);
        webSocketConsumersManager.resumeConsumer(viewId, userId);
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
