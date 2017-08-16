package com.dsc.wai.controller.api;

import com.dsc.wai.controller.BaseController;
import com.dsc.wai.model.User;
import com.dsc.wai.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles API requests.
 */
@Controller
@RequestMapping("/api")
public class ApiController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private LocationRepository locationRepository;

    /**
     * POST Requests handle submitting new user registration form.
     */
    @RequestMapping(path = "/users", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<User> retrieveUsers(@RequestParam(name = "limit", defaultValue = "1", required = false) Integer limit) {
        // Get logged in user.
        final long userId = getLoggedInUser().getUserId();

        if (limit > 10) {
            limit = 10;
        } else if (limit < 1) {
            limit = 1;
        }

        // Generate dummy data.
        final List<User> users = new ArrayList<>();
        for (int x = 0; x < limit; x++) {
            final User user = new User();
            user.setId(userId + x);
            users.add(user);
        }

        // return it, gets converted to JSON
        return users;
    }
}
