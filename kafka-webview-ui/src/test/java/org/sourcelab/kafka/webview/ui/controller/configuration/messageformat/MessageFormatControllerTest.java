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

package org.sourcelab.kafka.webview.ui.controller.configuration.messageformat;

import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
            //.andDo(print())
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
            //.andDo(print())
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

        // Define our expected json string
        final String expectedOptionsJson = "{\"option1\":\"value1\",\"option2\":\"value2\"}";

        // Hit index.
        mockMvc
            .perform(multipart("/configuration/messageFormat/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath)
                .param("customOptionNames", "option1")
                .param("customOptionNames", "option2")
                .param("customOptionValues", "value1")
                .param("customOptionValues", "value2"))
            //.andDo(print())
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/messageFormat"));

        // Validate
        final MessageFormat messageFormat = messageFormatRepository.findByName(expectedName);
        assertNotNull("Should have message format", messageFormat);
        assertEquals("Has correct name", expectedName, messageFormat.getName());
        assertEquals("Has correct classpath", expectedClassPath, messageFormat.getClasspath());
        assertNotNull("Has jar path", messageFormat.getJar());
        assertFalse("Should not be a default format", messageFormat.isDefaultFormat());

        // Validate that our options got set
        assertEquals("Got options", expectedOptionsJson, messageFormat.getOptionParameters());

        final boolean doesJarExist = Files.exists(Paths.get(deserializerUploadPath, messageFormat.getJar()));
        assertTrue("Deserializer file should have been uploaded", doesJarExist);

        // Cleanup
        Files.deleteIfExists(Paths.get(deserializerUploadPath, messageFormat.getJar()));
    }

    /**
     * Test attempting to create a new message format, but don't upload a jar.
     * This should be kicked back.
     */
    @Test
    @Transactional
    public void testPostUpdate_createNewMessageFormatMissingFile() throws Exception {
        final String expectedName = "MyMessageFormat" + System.currentTimeMillis();
        final String expectedClassPath = "examples.deserializer.ExampleDeserializer";

        final InputStream fileInputStream = null;
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/messageFormat/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            //.andDo(print())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("messageFormatForm", "file"))
            .andExpect(status().isOk());

        // Validate
        final MessageFormat messageFormat = messageFormatRepository.findByName(expectedName);
        assertNull("Should NOT have message format", messageFormat);
    }

    /**
     * Test attempting to create a new message format, but fail to load the deserializer.
     */
    @Test
    @Transactional
    public void testPostUpdate_createNewMessageFormatInvalidJar() throws Exception {
        final String expectedName = "MyMessageFormat" + System.currentTimeMillis();
        final String expectedClassPath = "examples.deserializer.ExampleDeserializer";

        // This isn't a real jar, will fail validation.
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "contents".getBytes(Charsets.UTF_8));

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/messageFormat/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            //.andDo(print())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("messageFormatForm", "file"))
            .andExpect(status().isOk());

        // Validate
        final MessageFormat messageFormat = messageFormatRepository.findByName(expectedName);
        assertNull("Should NOT have message format", messageFormat);
    }

    /**
     * Test attempting to update an existing message format, but send over a bad Id.
     * This should be kicked back as an error.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingButWithBadMessageFormatId() throws Exception {
        final String expectedName = "MyMessageFormat" + System.currentTimeMillis();
        final String expectedClassPath = "examples.deserializer.ExampleDeserializer";

        final MockMultipartFile jarUpload =
            new MockMultipartFile("file", "testPlugins.jar", null, "MyContent".getBytes(Charsets.UTF_8));

        // Hit page.
        final MvcResult result = mockMvc
            .perform(multipart("/configuration/messageFormat/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", "-1000")
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            //.andDo(print())
            .andExpect(flash().attributeExists("FlashMessage"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/messageFormat"))
            .andReturn();

        // Grab the flash message
        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        assertNotNull("Has flash message", flashMessage);
        assertTrue("Has error message", flashMessage.isWarning());
    }

    /**
     * Test attempting to update an existing message format, but upload a bad jar.
     * We'd expect it to be kicked back as an error, and keep the existing classpath and jar.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingButWithInvalidJar() throws Exception {
        final MessageFormat messageFormat = messageFormatTestTools.createMessageFormat("MyMessageFormat" + System.currentTimeMillis());
        final String expectedName = messageFormat.getName();
        final String expectedClasspath = messageFormat.getClasspath();
        final String expectedJarName = messageFormat.getJar();
        final String expectedJarContents = "OriginalContents";
        final String expectedParametersJson = messageFormat.getOptionParameters();
        final Path expectedJarPath = Paths.get(deserializerUploadPath, messageFormat.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(deserializerUploadPath + messageFormat.getJar(), expectedJarContents);

        // This is an invalid jar.
        final MockMultipartFile jarUpload =
            new MockMultipartFile("file", "testPlugins.jar", null, "MyContent".getBytes(Charsets.UTF_8));

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/messageFormat/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(messageFormat.getId()))
                .param("name", "Updated Name")
                .param("classpath", "made.up.classpath")
                .param("customOptionNames", "option1")
                .param("customOptionNames", "option2")
                .param("customOptionValues", "value1")
                .param("customOptionValues", "value2"))
            //.andDo(print())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("messageFormatForm", "file"))
            .andExpect(status().isOk());

        // Validate message format was not updated.
        final MessageFormat updatedMessageFormat = messageFormatRepository.findById(messageFormat.getId()).get();
        assertNotNull("Has message format", updatedMessageFormat);
        assertEquals("Name not updated", expectedName, updatedMessageFormat.getName());
        assertEquals("classpath not updated", expectedClasspath, updatedMessageFormat.getClasspath());
        assertEquals("Jar name not updated", expectedJarName, updatedMessageFormat.getJar());
        assertFalse("Should not be a default format", updatedMessageFormat.isDefaultFormat());
        assertEquals("Parameters not updated", expectedParametersJson, updatedMessageFormat.getOptionParameters());

        // Validate previous jar not overwritten.
        assertTrue("File should exist", Files.exists(expectedJarPath));
        final String jarContents = FileTestTools.readFile(expectedJarPath.toString());
        assertEquals("Jar contents should not have changed", expectedJarContents, jarContents);

        // Cleanup
        Files.deleteIfExists(expectedJarPath);
    }

    /**
     * Test attempting to update an existing message format, but not providing a jar.
     * We should change the name, but not touch the jar/classpath at all.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingNoJarUploaded() throws Exception {
        final MessageFormat messageFormat = messageFormatTestTools.createMessageFormat("MyMessageFormat" + System.currentTimeMillis());
        final String originalClasspath = messageFormat.getClasspath();
        final String originalJarName = messageFormat.getJar();
        final String originalJarContents = "OriginalContents";
        final Path originalJarPath = Paths.get(deserializerUploadPath, messageFormat.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(deserializerUploadPath + messageFormat.getJar(), originalJarContents);

        // This is the same as not uploading a jar.
        final InputStream emptyFile = null;
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, emptyFile);

        final String newName = "MyUpdatedName" + System.currentTimeMillis();
        final String newClasspath = "my new class path";

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/messageFormat/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(messageFormat.getId()))
                .param("name", newName)
                .param("classpath", newClasspath)
                .param("customOptionNames", "option1")
                .param("customOptionNames", "option2")
                .param("customOptionValues", "value1")
                .param("customOptionValues", "value2"))
            //.andDo(print())
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/messageFormat"));

        // Validate message format was updated.
        final MessageFormat updatedMessageFormat = messageFormatRepository.findById(messageFormat.getId()).get();
        assertNotNull("Has message format", updatedMessageFormat);
        assertEquals("Name updated", newName, updatedMessageFormat.getName());
        assertEquals("classpath was NOT updated", originalClasspath, updatedMessageFormat.getClasspath());
        assertNotEquals("classpath was NOT updated", newClasspath, updatedMessageFormat.getClasspath());
        assertEquals("Jar name not updated", originalJarName, updatedMessageFormat.getJar());
        assertFalse("Should not be a default format", updatedMessageFormat.isDefaultFormat());

        // Define our expected json string
        final String expectedOptionsJson = "{\"option1\":\"value1\",\"option2\":\"value2\"}";
        assertEquals("Parameters should have been updated", expectedOptionsJson, updatedMessageFormat.getOptionParameters());

        // Validate jar should still exist
        assertTrue("File should exist", Files.exists(originalJarPath));

        // Jar contents should be unchanged
        final String jarContents = FileTestTools.readFile(originalJarPath.toString());
        assertEquals("Jar contents should not have changed", originalJarContents, jarContents);

        // Cleanup
        Files.deleteIfExists(originalJarPath);
    }

    /**
     * Test attempting to update an existing message format, uploading a valid jar.
     * We change the name.  We expect the old file to be removed, and the new one added.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingWithValidJarSameName() throws Exception {
        final MessageFormat messageFormat = messageFormatTestTools.createMessageFormat("MyMessageFormat" + System.currentTimeMillis());
        final String originalJarName = messageFormat.getJar();
        final String originalJarContents = "OriginalContents";
        final Path originalJarPath = Paths.get(deserializerUploadPath, messageFormat.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(deserializerUploadPath + messageFormat.getJar(), originalJarContents);

        // This is a valid jar
        final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream("testDeserializer/testPlugins.jar");
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);

        final String newName = "MyUpdatedName" + System.currentTimeMillis();
        final String newClasspath = "examples.deserializer.ExampleDeserializer";

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/messageFormat/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(messageFormat.getId()))
                .param("name", newName)
                .param("classpath", newClasspath))
            //.andDo(print())
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/messageFormat"));

        // Validate message format was updated.
        final MessageFormat updatedMessageFormat = messageFormatRepository.findById(messageFormat.getId()).get();
        assertNotNull("Has message format", updatedMessageFormat);
        assertEquals("Name updated", newName, updatedMessageFormat.getName());
        assertEquals("classpath updated", newClasspath, updatedMessageFormat.getClasspath());
        assertNotEquals("Jar name not updated", originalJarName, updatedMessageFormat.getJar());
        assertFalse("Should not be a default format", updatedMessageFormat.isDefaultFormat());

        // No parameters were posted, so we should have an empty json parameters
        assertEquals("No parameters should be empty", "{}", updatedMessageFormat.getOptionParameters());

        // Validate previous jar is gone/deleted.
        assertFalse("File should NOT exist", Files.exists(originalJarPath));

        // Validate new jar is created.
        final Path newJarPath = Paths.get(deserializerUploadPath, updatedMessageFormat.getJar());
        assertTrue("New jar should exist", Files.exists(newJarPath));

        // Cleanup
        Files.deleteIfExists(newJarPath);
    }

    /**
     * Test loading edit for existing message format.
     */
    @Test
    @Transactional
    public void testGetEdit_existingMessageFormat() throws Exception {
        final MessageFormat format = messageFormatTestTools.createMessageFormat("MyMessageFormat" + System.currentTimeMillis());
        format.setOptionParameters("{\"myOption1\":\"myValue1\",\"myOption2\":\"myValue2\"}");
        messageFormatRepository.save(format);

        // Hit edit page.
        mockMvc
            .perform(get("/configuration/messageFormat/edit/" + format.getId())
                .with(user(adminUserDetails)))
            //.andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(format.getName())))
            .andExpect(content().string(containsString(format.getClasspath())))

            // Should have our Id input
            .andExpect(content().string(containsString("value=\"" + format.getId() + "\"")))

            // Should have our defined options
            .andExpect(content().string(containsString("myValue1")))
            .andExpect(content().string(containsString("myValue2")))
            .andExpect(content().string(containsString("myOption1")))
            .andExpect(content().string(containsString("myOption2")));
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
            //.andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/messageFormat"));

        // Validate
        assertFalse("Should NOT have message format", messageFormatRepository.existsById(formatId));

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

        // Create a Views that uses our format.
        final View view1 = viewTestTools.createViewWithFormat("My Name", format);
        final View view2 = viewTestTools.createViewWithFormat("My Other Name", format);

        // Generate a dummy file
        final Path expectedJarPath = Paths.get(deserializerUploadPath, format.getJar());
        FileTestTools.createDummyFile(expectedJarPath.toString(), "MyContests");
        assertTrue("Sanity test", Files.exists(expectedJarPath));

        final String expectedError = "Message Format in use by View: [My Name, My Other Name]";

        // Hit index.
        final MvcResult result = mockMvc
            .perform(post("/configuration/messageFormat/delete/" + formatId)
                .with(user(adminUserDetails))
                .with(csrf()))
            //.andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/messageFormat"))
            .andReturn();

        final FlashMessage flashMessage = (FlashMessage) result.getFlashMap().get("FlashMessage");
        assertNotNull("Should have a flash message set", flashMessage);
        assertTrue("Should be warning", flashMessage.isWarning());
        assertEquals("Should have our error msg", expectedError, flashMessage.getMessage());

        // Validate
        final MessageFormat messageFormat = messageFormatRepository.findById(formatId).get();
        assertNotNull("Should NOT have removed message format", messageFormat);

        // Jar should still exist
        assertTrue("Jar should not have been removed", Files.exists(expectedJarPath));

        // Cleanup
        Files.deleteIfExists(expectedJarPath);
    }
}