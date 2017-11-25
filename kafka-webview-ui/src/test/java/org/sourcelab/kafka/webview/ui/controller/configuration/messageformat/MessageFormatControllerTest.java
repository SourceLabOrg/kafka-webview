package org.sourcelab.kafka.webview.ui.controller.configuration.messageformat;

import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.configuration.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.sourcelab.kafka.webview.ui.tools.FileTestTools;
import org.sourcelab.kafka.webview.ui.tools.MessageFormatTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MessageFormatControllerTest extends AbstractMvcTest {

    @Autowired
    private MessageFormatTestTools messageFormatTestTools;

    @Autowired
    private MessageFormatRepository messageFormatRepository;

    @Autowired
    private ViewTestTools viewTestTools;

    /**
     * Where deserializer files are uploaded to.
     */
    private String deserializerUploadPath;

    @Before
    public void setupUploadPath() {
        deserializerUploadPath = uploadPath + "/deserializers/";
    }

    /**
     * Test cannot load pages w/o admin role.
     */
    @Test
    @Transactional
    public void test_withoutAdminRole() throws Exception {
        testUrlWithOutAdminRole("/configuration/messageFormat", false);
        testUrlWithOutAdminRole("/configuration/messageFormat/create", false);
        testUrlWithOutAdminRole("/configuration/messageFormat/edit/1", false);
        testUrlWithOutAdminRole("/configuration/messageFormat/create", true);
        testUrlWithOutAdminRole("/configuration/messageFormat/delete/1", true);
    }

    /**
     * Smoke test the message format Index page.
     */
    @Test
    @Transactional
    public void testIndex() throws Exception {
        // Create some dummy formats
        final MessageFormat format1 = messageFormatTestTools.createMessageFormat("Format 1");
        final MessageFormat format2 = messageFormatTestTools.createMessageFormat("Format 2");

        // Hit index.
        mockMvc
            .perform(get("/configuration/messageFormat").with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Validate cluster 1
            .andExpect(content().string(containsString(format1.getName())))
            .andExpect(content().string(containsString(format1.getClasspath())))

            // Validate cluster 2
            .andExpect(content().string(containsString(format2.getName())))
            .andExpect(content().string(containsString(format2.getClasspath())));
    }

    /**
     * Smoke test the message format create form.
     */
    @Test
    @Transactional
    public void testGetCreate() throws Exception {
        // Hit index.
        mockMvc
            .perform(get("/configuration/messageFormat/create").with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    /**
     * Smoke test creating new message format.
     */
    @Test
    @Transactional
    public void testPostUpdate_newMessageFormat() throws Exception {
        final String expectedName = "MyMessageFormat" + System.currentTimeMillis();
        final String expectedClassPath = "examples.deserializer.ExampleDeserializer";

        final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream("testDeserializer/testPlugins.jar");
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);

        // Hit index.
        mockMvc
            .perform(fileUpload("/configuration/messageFormat/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/messageFormat"));

        // Validate
        final MessageFormat messageFormat = messageFormatRepository.findByName(expectedName);
        assertNotNull("Should have message format", messageFormat);
        assertEquals("Has correct name", expectedName, messageFormat.getName());
        assertEquals("Has correct classpath", expectedClassPath, messageFormat.getClasspath());
        assertNotNull("Has jar path", messageFormat.getJar());

        final boolean doesJarExist = Files.exists(Paths.get(deserializerUploadPath, messageFormat.getJar()));
        assertTrue("Deserializer file should have been uploaded", doesJarExist);

        // Cleanup
        Files.deleteIfExists(Paths.get(deserializerUploadPath, messageFormat.getJar()));
    }

    /**
     * Test deleting a message format that has no usages.
     */
    @Test
    @Transactional
    public void testPostDelete_notUsed() throws Exception {
        // Create some dummy formats
        final MessageFormat format = messageFormatTestTools.createMessageFormat("Format 1");
        final long formatId = format.getId();

        // Generate a dummy file
        final Path expectedJarPath = Paths.get(deserializerUploadPath, format.getJar());
        FileTestTools.createDummyFile(expectedJarPath.toString(), "MyContests");
        assertTrue("Sanity test", Files.exists(expectedJarPath));

        // Hit index.
        mockMvc
            .perform(post("/configuration/messageFormat/delete/" + formatId)
                .with(user(adminUserDetails))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/messageFormat"));

        // Validate
        final MessageFormat messageFormat = messageFormatRepository.findOne(formatId);
        assertNull("Should NOT have message format", messageFormat);

        // Jar should have been removed
        assertFalse("Should have been removed", Files.exists(expectedJarPath));
    }

    /**
     * Test deleting a message format that is being used by a view.
     * It should block deleting.
     */
    @Test
    @Transactional
    public void testPostDelete_inUse() throws Exception {
        // Create some dummy formats
        final MessageFormat format = messageFormatTestTools.createMessageFormat("Format 1");
        final long formatId = format.getId();

        // Create a View that uses our format.
        final View view = viewTestTools.createViewWithFormat("My Name", format);

        // Generate a dummy file
        final Path expectedJarPath = Paths.get(deserializerUploadPath, format.getJar());
        FileTestTools.createDummyFile(expectedJarPath.toString(), "MyContests");
        assertTrue("Sanity test", Files.exists(expectedJarPath));

        // Hit index.
        mockMvc
            .perform(post("/configuration/messageFormat/delete/" + formatId)
                .with(user(adminUserDetails))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/messageFormat"));

        // Validate
        final MessageFormat messageFormat = messageFormatRepository.findOne(formatId);
        assertNotNull("Should NOT have removed message format", messageFormat);

        // Jar should still exist
        assertTrue("Jar should not have been removed", Files.exists(expectedJarPath));

        // Cleanup
        Files.deleteIfExists(expectedJarPath);
    }
}