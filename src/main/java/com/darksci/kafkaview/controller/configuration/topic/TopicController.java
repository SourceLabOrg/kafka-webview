package com.darksci.kafkaview.controller.configuration.topic;

import com.darksci.kafkaview.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/configuration/topic")
public class TopicController extends BaseController {

    /**
     * GET Displays main configuration index.
     */
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String index(final Model model) {
        return "configuration/topic/index";
    }
}
