package com.darksci.kafka.webview.ui.controller.stream;

import com.darksci.kafka.webview.ui.controller.BaseController;
import com.darksci.kafka.webview.ui.controller.api.ConsumeRequest;
import com.darksci.kafka.webview.ui.manager.kafka.SessionIdentifier;
import com.darksci.kafka.webview.ui.manager.kafka.ViewCustomizer;
import com.darksci.kafka.webview.ui.manager.kafka.config.FilterDefinition;
import com.darksci.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
import com.darksci.kafka.webview.ui.manager.ui.BreadCrumbManager;
import com.darksci.kafka.webview.ui.manager.ui.FlashMessage;
import com.darksci.kafka.webview.ui.manager.user.CustomUserDetails;
import com.darksci.kafka.webview.ui.model.View;
import com.darksci.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Web socket controller end points.
 */
@Controller
@RequestMapping("/stream")
public class StreamController extends BaseController {
    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private WebSocketConsumersManager webSocketConsumersManager;

    /**
     * Just redirects to view index for now.
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
        @PathVariable final Long id,
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
        @DestinationVariable final Long viewId,
        final ConsumeRequest consumeRequest,
        final SimpMessageHeaderAccessor headerAccessor) {

        // Retrieve view
        final View view = viewRepository.findOne(viewId);
        if (view == null) {
            return "{success: false}";
        }

        // Build a session identifier
        final long userId = getLoggedInUserId(headerAccessor);
        final String sessionId = headerAccessor.getSessionId();
        final SessionIdentifier sessionIdentifier = new SessionIdentifier(userId, sessionId);

        // Override settings
        final ViewCustomizer viewCustomizer = new ViewCustomizer(view, consumeRequest);
        viewCustomizer.overrideViewSettings();
        final List<FilterDefinition> configuredFilters = viewCustomizer.getFilterDefinitions();

        webSocketConsumersManager.addNewConsumer(view, configuredFilters, sessionIdentifier);
        return "{success: true}";
    }

    /**
     * Serves web socket requests, requesting to pause a consumer.
     */
    @MessageMapping("/pause/{viewId}")
    @Transactional
    public String pauseConsumer(
        @DestinationVariable final Long viewId,
        final SimpMessageHeaderAccessor headerAccessor) {

        // Request a pause
        final long userId = getLoggedInUserId(headerAccessor);
        final String sessionId = headerAccessor.getSessionId();
        webSocketConsumersManager.pauseConsumer(viewId, new SessionIdentifier(userId, sessionId));
        return "{success: true}";
    }

    /**
     * Serves web socket requests, requesting to resume a consumer.
     */
    @MessageMapping("/resume/{viewId}")
    @Transactional
    public String resumeConsumer(
        @DestinationVariable final Long viewId,
        final SimpMessageHeaderAccessor headerAccessor) {

        // Request Resume
        final long userId = getLoggedInUserId(headerAccessor);
        final String sessionId = headerAccessor.getSessionId();
        webSocketConsumersManager.resumeConsumer(viewId, new SessionIdentifier(userId, sessionId));
        return "{success: true}";
    }

    /**
     * @return Currently logged in user Id.
     */
    private long getLoggedInUserId(final SimpMessageHeaderAccessor headerAccessor) {
        return getLoggedInUser(headerAccessor).getUserId();
    }

    /**
     * @return Currently logged in user's details.
     */
    private CustomUserDetails getLoggedInUser(final SimpMessageHeaderAccessor headerAccessor) {
        return (CustomUserDetails) ((Authentication)headerAccessor.getUser()).getPrincipal();
    }
}
