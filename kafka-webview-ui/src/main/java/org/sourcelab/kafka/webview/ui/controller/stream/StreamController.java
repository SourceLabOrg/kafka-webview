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

package org.sourcelab.kafka.webview.ui.controller.stream;

import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.api.requests.ConsumeRequest;
import org.sourcelab.kafka.webview.ui.manager.kafka.SessionIdentifier;
import org.sourcelab.kafka.webview.ui.manager.kafka.ViewCustomizer;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterDefinition;
import org.sourcelab.kafka.webview.ui.manager.socket.StartingPosition;
import org.sourcelab.kafka.webview.ui.manager.socket.WebSocketConsumersManager;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.manager.user.CustomUserDetails;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
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
import java.util.Optional;

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
        final Optional<View> viewOptional = viewRepository.findById(id);
        if (!viewOptional.isPresent()) {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find view!"));

            // redirect to home
            return "redirect:/";
        }
        final View view = viewOptional.get();

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
        final Optional<View> viewOptional = viewRepository.findById(viewId);
        if (!viewOptional.isPresent()) {
            return "{success: false}";
        }
        final View view = viewOptional.get();

        // Build a session identifier
        final long userId = getLoggedInUserId(headerAccessor);
        final String sessionId = headerAccessor.getSessionId();
        final SessionIdentifier sessionIdentifier = SessionIdentifier.newStreamIdentifier(userId, sessionId);

        // Override settings
        final ViewCustomizer viewCustomizer = new ViewCustomizer(view, consumeRequest);
        viewCustomizer.overrideViewSettings();
        final List<FilterDefinition> configuredFilters = viewCustomizer.getFilterDefinitions();

        // Configure where to start from
        final StartingPosition startingPosition = viewCustomizer.getStartingPosition();

        webSocketConsumersManager.addNewConsumer(view, configuredFilters, startingPosition, sessionIdentifier);
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
        webSocketConsumersManager.pauseConsumer(viewId, SessionIdentifier.newStreamIdentifier(userId, sessionId));
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
        webSocketConsumersManager.resumeConsumer(viewId, SessionIdentifier.newStreamIdentifier(userId, sessionId));
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
