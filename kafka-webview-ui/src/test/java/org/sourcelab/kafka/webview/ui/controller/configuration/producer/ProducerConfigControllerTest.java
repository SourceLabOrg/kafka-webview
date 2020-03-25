/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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
package org.sourcelab.kafka.webview.ui.controller.configuration.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.model.Producer;
import org.sourcelab.kafka.webview.ui.tools.ProducerTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith( SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProducerConfigControllerTest extends AbstractMvcTest
{
    @Autowired
    private ProducerTestTools producerTestTools;

    /**
     * Test cannot load pages w/o admin role.
     */
    @Test
    @Transactional
    public void test_withoutAdminRole() throws Exception {
        testUrlWithOutAdminRole("/configuration/producer", false);
        testUrlWithOutAdminRole("/configuration/producer/create", false);
        testUrlWithOutAdminRole("/configuration/producer/update", true);
        testUrlWithOutAdminRole("/configuration/producer/delete/1", true);
    }

    /**
     * Smoke test the Producer Index page
     */
    @Test
    @Transactional
    public void testIndex() throws Exception {
        final Producer producer = producerTestTools.createProducer( true );

        // Hit index.
        mockMvc
            .perform(get("/configuration/producer").with(user(adminUserDetails)))
            //.andDo(print())
            .andExpect(status().isOk())
            // Validate view 1
            .andExpect(content().string(containsString(producer.getName())));

    }

    /**
     * Smoke test the Producer create page
     */
    @Test
    @Transactional
    public void testCreate() throws Exception {
        // Hit index.
        mockMvc
            .perform(get("/configuration/producer/create").with(user(adminUserDetails)))
            //.andDo(print())
            .andExpect(status().isOk())

            // Validate submit button seems to show up.
            .andExpect(content().string(containsString("type=\"submit\"")));
    }
}
