package com.darksci.kafkaview.controller.setup;

import com.darksci.kafkaview.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/setup")
public class SetupController extends BaseController {
    private final static Logger logger = LoggerFactory.getLogger(SetupController.class);

    /**
     * GET Displays main setup index.
     */
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String index() {
        return "setup/index";
    }


}
