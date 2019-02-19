package org.sourcelab.kafka.webview.ui.controller.stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StreamControllerTest extends AbstractMvcTest {

    @Autowired
    private ViewTestTools viewTestTools;

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        final View view = viewTestTools.createView("TestView");

        // View "stream" page.
        testUrlRequiresAuthentication("/stream/" + view.getId(), false);
    }

    /**
     * Smoke test stream page loads.  Does no real inspection that the page functions beyond
     * simply loading.
     */
    @Test
    @Transactional
    public void smokeTestStream() throws Exception {
        final View view = viewTestTools.createView("TestView");

        // Hit the stream page for specified view.
        mockMvc
            .perform(get("/stream/" + view.getId())
                .with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Contains some basic text
            .andExpect(content().string(containsString(view.getName())))
            .andExpect(content().string(containsString(view.getTopic())))
            .andExpect(content().string(containsString("Switch to View")))
            .andExpect(content().string(containsString("href=\"/view/" + view.getId() + "\"")));
    }
}
