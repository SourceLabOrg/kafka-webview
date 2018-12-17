package org.sourcelab.kafka.webview.ui.controller.login;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies no login required when authentication disabled.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(value = {"app.user.enabled=false"})
@AutoConfigureMockMvc
public class AnonymousLoginTest {
    @Autowired
    protected MockMvc mockMvc;

    /**
     * Attempt to retrieve a random page in the app and verify we're logged in as an anonymous admin user.
     */
    @Test
    public void test_validAnonymousUser() throws Exception {
        // Attempt to login now
        final MvcResult result = mockMvc
            .perform(get("/configuration"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    }

}

