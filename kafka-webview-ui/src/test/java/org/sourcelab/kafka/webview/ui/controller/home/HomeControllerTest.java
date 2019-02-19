package org.sourcelab.kafka.webview.ui.controller.home;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HomeControllerTest extends AbstractMvcTest {

    @Autowired
    private ViewTestTools viewTestTools;

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        // home page.
        testUrlRequiresAuthentication("/", false);

        // help page.
        testUrlRequiresAuthentication("/help", false);
    }

    /**
     * Smoke test / index page loads without error for a fresh install with no data.
     * It should load the help page.
     */
    @Test
    @Transactional
    public void smokeTestIndexPageWithNothingSetupYet() throws Exception {
        // Hit the index page.
        mockMvc
            .perform(get("/")
                .with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Basic text validations
            .andExpect(content().string(containsString("Let's get started!")))
            .andExpect(content().string(containsString("Looks like you've just installed Kafka WebView")))
            .andExpect(content().string(containsString("Custom Deserializers")));
    }

    /**
     * Smoke test / index page loads without error with a view already created.
     * Should redirect to view index.
     */
    @Test
    @Transactional
    public void smokeTestIndexPageWithViewCreated() throws Exception {
        // Create a view
        final String view1Name = "My View Name";
        viewTestTools.createView(view1Name);

        // Hit the index page.
        mockMvc
            .perform(get("/")
                .with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/view*"));
    }

    /**
     * Smoke test /help page loads without error.
     */
    @Test
    @Transactional
    public void smokeTestHelpPage() throws Exception {
        // Hit the help page.
        mockMvc
            .perform(get("/help")
                .with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Basic text validations
            .andExpect(content().string(containsString("Documentation and Help")))
            .andExpect(content().string(containsString("Custom Deserializers")));
    }
}
