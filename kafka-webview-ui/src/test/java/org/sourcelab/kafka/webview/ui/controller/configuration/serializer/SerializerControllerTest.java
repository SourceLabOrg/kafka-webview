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

package org.sourcelab.kafka.webview.ui.controller.configuration.serializer;

import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.kafka.producer.transformer.LongTransformer;
import org.sourcelab.kafka.webview.ui.manager.kafka.producer.transformer.StringTransformer;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.SerializerFormat;
import org.sourcelab.kafka.webview.ui.repository.SerializerFormatRepository;
import org.sourcelab.kafka.webview.ui.tools.FileTestTools;
import org.sourcelab.kafka.webview.ui.tools.SerializerFormatTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SerializerControllerTest extends AbstractMvcTest {
    /**
     * Bae path to hit this controller.
     */
    private static final String controllerPath = "/configuration/serializer";

    /**
     * Attribute name of the form instance.
     */
    private static final String formAttributeName = "serializerForm";

    @Autowired
    private SerializerFormatTestTools entityTestTools;

    @Autowired
    private SerializerFormatRepository entityRepository;

    /**
     * Where Partitioners files are uploaded to.
     */
    private String entityUploadPath;

    @Before
    public void setupUploadPath() {
        entityUploadPath = uploadPath + "/serializers/";
    }

    /**
     * Test cannot load pages w/o admin role.
     */
    @Test
    @Transactional
    public void test_withoutAdminRole() throws Exception {
        testUrlWithOutAdminRole(controllerPath, false);
        testUrlWithOutAdminRole(controllerPath + "/create", false);
        testUrlWithOutAdminRole(controllerPath + "/edit/1", false);
        testUrlWithOutAdminRole(controllerPath + "/create", true);
        testUrlWithOutAdminRole(controllerPath + "/delete/1", true);
    }

    /**
     * Smoke test the partitioning strategy Index page.
     */
    @Test
    @Transactional
    public void testIndex() throws Exception {
        // Create some custom formats
        final SerializerFormat serializerFormat1 = entityTestTools.createStrategy("Strategy 1");
        final SerializerFormat serializerFormat2 = entityTestTools.createStrategy("Strategy 2");

        // Check for default instance
        final Map<String, String> expectedDefaultInstances = new HashMap<>();
        expectedDefaultInstances.put("String Serializer", StringTransformer.class.getName());
        expectedDefaultInstances.put("Long Serializer", LongTransformer.class.getName());

        // Hit index.
        final MvcResult result = mockMvc
            .perform(get(controllerPath).with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Validate instance 1
            .andExpect(content().string(containsString(serializerFormat1.getName())))
            .andExpect(content().string(containsString(serializerFormat1.getClasspath())))

            // Validate instance 2
            .andExpect(content().string(containsString(serializerFormat2.getName())))
            .andExpect(content().string(containsString(serializerFormat2.getClasspath())))
            .andReturn();

        // Validate default instances
        final String responseContent = result.getResponse().getContentAsString();
        for (final Map.Entry<String, String> expectedEntry : expectedDefaultInstances.entrySet()) {
            assertTrue("Content should contain " + expectedEntry.getKey(), responseContent.contains(expectedEntry.getKey()));
            assertTrue("Content should contain " + expectedEntry.getValue(), responseContent.contains(expectedEntry.getValue()));
        }
    }

    /**
     * Smoke test the partitioning strategy create form.
     */
    @Test
    @Transactional
    public void testGetCreate() throws Exception {
        // Hit index.
        mockMvc
            .perform(get(controllerPath + "/create").with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    /**
     * Smoke test creating new partitioning strategy.
     */
    @Test
    @Transactional
    public void testPostUpdate_newSerializerFormat() throws Exception {
        final String expectedName = "MyPartitioner" + System.currentTimeMillis();
        final String expectedClassPath = "examples.partitioner.StaticPartitioner";

        final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream("testDeserializer/testPlugins.jar");
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);

        // Define our expected json string
        final String expectedOptionsJson = "{\"option1\":\"value1\",\"option2\":\"value2\"}";

        // Hit index.
        mockMvc
            .perform(multipart(controllerPath + "/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath)
                .param("customOptionNames", "option1")
                .param("customOptionNames", "option2")
                .param("customOptionValues", "value1")
                .param("customOptionValues", "value2"))
            .andDo(print())
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(controllerPath));

        // Validate
        final SerializerFormat serializerFormat = entityRepository.findByName(expectedName);
        assertNotNull("Should have partitioning strategy", serializerFormat);
        assertEquals("Has correct name", expectedName, serializerFormat.getName());
        assertEquals("Has correct classpath", expectedClassPath, serializerFormat.getClasspath());
        assertNotNull("Has jar path", serializerFormat.getJar());
        assertFalse("Should not be a default format", serializerFormat.isDefault());

        // Validate that our options got set
        assertEquals("Got options", expectedOptionsJson, serializerFormat.getOptionParameters());

        final boolean doesJarExist = Files.exists(Paths.get(entityUploadPath, serializerFormat.getJar()));
        assertTrue("Partitioner jar file should have been uploaded", doesJarExist);

        // Cleanup
        Files.deleteIfExists(Paths.get(entityUploadPath, serializerFormat.getJar()));
    }

    /**
     * Test attempting to create a new partitioning strategy, but don't upload a jar.
     * This should be kicked back.
     */
    @Test
    @Transactional
    public void testPostUpdate_createNewMessageFormatMissingFile() throws Exception {
        final String expectedName = "MyPartitioner" + System.currentTimeMillis();
        final String expectedClassPath = "examples.partitioner.StaticPartitioner";

        final InputStream fileInputStream = null;
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);

        // Hit page.
        mockMvc
            .perform(multipart(controllerPath + "/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            .andDo(print())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors(formAttributeName, "file"))
            .andExpect(status().isOk());

        // Validate
        final SerializerFormat messageFormat = entityRepository.findByName(expectedName);
        assertNull("Should NOT have partitioning strategy", messageFormat);
    }

    /**
     * Test attempting to create a new partitioning strategy, but fail to load the deserializer.
     */
    @Test
    @Transactional
    public void testPostUpdate_createNewMessageFormatInvalidJar() throws Exception {
        final String expectedName = "MyPartitioner" + System.currentTimeMillis();
        final String expectedClassPath = "examples.partitioner.StaticPartitioner";

        // This isn't a real jar, will fail validation.
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "contents".getBytes(Charsets.UTF_8));

        // Hit page.
        mockMvc
            .perform(multipart(controllerPath + "/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            .andDo(print())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors(formAttributeName, "file"))
            .andExpect(status().isOk());

        // Validate
        final SerializerFormat messageFormat = entityRepository.findByName(expectedName);
        assertNull("Should NOT have partitioning strategy", messageFormat);
    }

    /**
     * Test attempting to update an existing partitioning strategy, but send over a bad Id.
     * This should be kicked back as an error.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingButWithBadMessageFormatId() throws Exception {
        final String expectedName = "MyPartitioner" + System.currentTimeMillis();
        final String expectedClassPath = "examples.partitioner.StaticPartitioner";

        final MockMultipartFile jarUpload =
            new MockMultipartFile("file", "testPlugins.jar", null, "MyContent".getBytes(Charsets.UTF_8));

        // Hit page.
        final MvcResult result = mockMvc
            .perform(multipart(controllerPath + "/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", "-1000")
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            .andDo(print())
            .andExpect(flash().attributeExists("FlashMessage"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/partitionStrategy"))
            .andReturn();

        // Grab the flash message
        final FlashMessage flashMessage = (FlashMessage) result
            .getFlashMap()
            .get("FlashMessage");

        assertNotNull("Has flash message", flashMessage);
        assertTrue("Has error message", flashMessage.isWarning());
    }

    /**
     * Test attempting to update an existing partitioning strategy, but upload a bad jar.
     * We'd expect it to be kicked back as an error, and keep the existing classpath and jar.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingButWithInvalidJar() throws Exception {
        final SerializerFormat serializerFormat = entityTestTools.createStrategy("MyPartitioner" + System.currentTimeMillis());
        final String expectedName = serializerFormat.getName();
        final String expectedClasspath = serializerFormat.getClasspath();
        final String expectedJarName = serializerFormat.getJar();
        final String expectedJarContents = "OriginalContents";
        final String expectedParametersJson = serializerFormat.getOptionParameters();
        final Path expectedJarPath = Paths.get(entityUploadPath, serializerFormat.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(entityUploadPath + serializerFormat.getJar(), expectedJarContents);

        // This is an invalid jar.
        final MockMultipartFile jarUpload =
            new MockMultipartFile("file", "testPlugins.jar", null, "MyContent".getBytes(Charsets.UTF_8));

        // Hit page.
        mockMvc
            .perform(multipart(controllerPath + "/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(serializerFormat.getId()))
                .param("name", "Updated Name")
                .param("classpath", "made.up.classpath")
                .param("customOptionNames", "option1")
                .param("customOptionNames", "option2")
                .param("customOptionValues", "value1")
                .param("customOptionValues", "value2"))
            .andDo(print())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors(formAttributeName, "file"))
            .andExpect(status().isOk());

        // Validate partitioning strategy was not updated.
        final SerializerFormat updatedSerializerFormat = entityRepository.findById(serializerFormat.getId()).get();
        assertNotNull("Has partitioning strategy", updatedSerializerFormat);
        assertEquals("Name not updated", expectedName, updatedSerializerFormat.getName());
        assertEquals("classpath not updated", expectedClasspath, updatedSerializerFormat.getClasspath());
        assertEquals("Jar name not updated", expectedJarName, updatedSerializerFormat.getJar());
        assertFalse("Should not be a default format", updatedSerializerFormat.isDefault());
        assertEquals("Parameters not updated", expectedParametersJson, updatedSerializerFormat.getOptionParameters());

        // Validate previous jar not overwritten.
        assertTrue("File should exist", Files.exists(expectedJarPath));
        final String jarContents = FileTestTools.readFile(expectedJarPath.toString());
        assertEquals("Jar contents should not have changed", expectedJarContents, jarContents);

        // Cleanup
        Files.deleteIfExists(expectedJarPath);
    }

    /**
     * Test attempting to update an existing partitioning strategy, but not providing a jar.
     * We should change the name, but not touch the jar/classpath at all.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingNoJarUploaded() throws Exception {
        final SerializerFormat serializerFormat = entityTestTools.createStrategy("myPatitioner" + System.currentTimeMillis());
        final String originalClasspath = serializerFormat.getClasspath();
        final String originalJarName = serializerFormat.getJar();
        final String originalJarContents = "OriginalContents";
        final Path originalJarPath = Paths.get(entityUploadPath, serializerFormat.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(entityUploadPath + serializerFormat.getJar(), originalJarContents);

        // This is the same as not uploading a jar.
        final InputStream emptyFile = null;
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, emptyFile);

        final String newName = "MyUpdatedName" + System.currentTimeMillis();
        final String newClasspath = "my new class path";

        // Hit page.
        mockMvc
            .perform(multipart(controllerPath + "/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(serializerFormat.getId()))
                .param("name", newName)
                .param("classpath", newClasspath)
                .param("customOptionNames", "option1")
                .param("customOptionNames", "option2")
                .param("customOptionValues", "value1")
                .param("customOptionValues", "value2"))
            .andDo(print())
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(controllerPath));

        // Validate partitioning strategy was updated.
        final SerializerFormat updatedSerializerFormat = entityRepository.findById(serializerFormat.getId()).get();
        assertNotNull("Has partitioning strategy", updatedSerializerFormat);
        assertEquals("Name updated", newName, updatedSerializerFormat.getName());
        assertEquals("classpath was NOT updated", originalClasspath, updatedSerializerFormat.getClasspath());
        assertNotEquals("classpath was NOT updated", newClasspath, updatedSerializerFormat.getClasspath());
        assertEquals("Jar name not updated", originalJarName, updatedSerializerFormat.getJar());
        assertFalse("Should not be a default format", updatedSerializerFormat.isDefault());

        // Define our expected json string
        final String expectedOptionsJson = "{\"option1\":\"value1\",\"option2\":\"value2\"}";
        assertEquals("Parameters should have been updated", expectedOptionsJson, updatedSerializerFormat.getOptionParameters());

        // Validate jar should still exist
        assertTrue("File should exist", Files.exists(originalJarPath));

        // Jar contents should be unchanged
        final String jarContents = FileTestTools.readFile(originalJarPath.toString());
        assertEquals("Jar contents should not have changed", originalJarContents, jarContents);

        // Cleanup
        Files.deleteIfExists(originalJarPath);
    }

    /**
     * Test attempting to update an existing partitioning strategy, uploading a valid jar.
     * We change the name.  We expect the old file to be removed, and the new one added.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingWithValidJarSameName() throws Exception {
        final SerializerFormat serializerFormat = entityTestTools.createStrategy("MyPartitioner" + System.currentTimeMillis());
        final String originalJarName = serializerFormat.getJar();
        final String originalJarContents = "OriginalContents";
        final Path originalJarPath = Paths.get(entityUploadPath, serializerFormat.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(entityUploadPath + serializerFormat.getJar(), originalJarContents);

        // This is a valid jar
        final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream("testDeserializer/testPlugins.jar");
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);

        final String newName = "MyUpdatedName" + System.currentTimeMillis();
        final String newClasspath = "examples.partitioner.StaticPartitioner";

        // Hit page.
        mockMvc
            .perform(multipart(controllerPath + "/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(serializerFormat.getId()))
                .param("name", newName)
                .param("classpath", newClasspath))
            .andDo(print())
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(controllerPath));

        // Validate partitioning strategy was updated.
        final SerializerFormat updatedPartitionStrategy = entityRepository.findById(serializerFormat.getId()).get();
        assertNotNull("Has partitioning strategy", updatedPartitionStrategy);
        assertEquals("Name updated", newName, updatedPartitionStrategy.getName());
        assertEquals("classpath updated", newClasspath, updatedPartitionStrategy.getClasspath());
        assertNotEquals("Jar name not updated", originalJarName, updatedPartitionStrategy.getJar());
        assertFalse("Should not be a default format", updatedPartitionStrategy.isDefault());

        // No parameters were posted, so we should have an empty json parameters
        assertEquals("No parameters should be empty", "{}", updatedPartitionStrategy.getOptionParameters());

        // Validate previous jar is gone/deleted.
        assertFalse("File should NOT exist", Files.exists(originalJarPath));

        // Validate new jar is created.
        final Path newJarPath = Paths.get(entityUploadPath, updatedPartitionStrategy.getJar());
        assertTrue("New jar should exist", Files.exists(newJarPath));

        // Cleanup
        Files.deleteIfExists(newJarPath);
    }

    /**
     * Test loading edit for existing partitioning strategy.
     */
    @Test
    @Transactional
    public void testGetEdit_existingMessageFormat() throws Exception {
        final SerializerFormat serializerFormat = entityTestTools.createStrategy("My Partitioner" + System.currentTimeMillis());
        serializerFormat.setOptionParameters("{\"myOption1\":\"myValue1\",\"myOption2\":\"myValue2\"}");
        entityRepository.save(serializerFormat);

        // Hit edit page.
        mockMvc
            .perform(get(controllerPath + "/edit/" + serializerFormat.getId())
                .with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(serializerFormat.getName())))
            .andExpect(content().string(containsString(serializerFormat.getClasspath())))

            // Should have our Id input
            .andExpect(content().string(containsString("value=\"" + serializerFormat.getId() + "\"")))

            // Should have our defined options
            .andExpect(content().string(containsString("myValue1")))
            .andExpect(content().string(containsString("myValue2")))
            .andExpect(content().string(containsString("myOption1")))
            .andExpect(content().string(containsString("myOption2")));
    }

    /**
     * Test deleting a partitioning strategy that has no usages.
     */
    @Test
    @Transactional
    public void testPostDelete_notUsed() throws Exception {
        // Create some dummy formats
        final SerializerFormat serializerFormat = entityTestTools.createStrategy("Strategy 2");
        final long formatId = serializerFormat.getId();

        // Generate a dummy file
        final Path expectedJarPath = Paths.get(entityUploadPath, serializerFormat.getJar());
        FileTestTools.createDummyFile(expectedJarPath.toString(), "MyContents");
        assertTrue("Sanity test", Files.exists(expectedJarPath));

        // Hit index.
        mockMvc
            .perform(post(controllerPath + "/delete/" + formatId)
                .with(user(adminUserDetails))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(controllerPath));

        // Validate
        assertFalse("Should NOT have partitioning strategy", entityRepository.existsById(formatId));

        // Jar should have been removed
        assertFalse("Should have been removed", Files.exists(expectedJarPath));
    }

    /**
     * Test deleting a partitioning strategy that is being used by a XXX.
     * It should block deleting.
     *
     * TODO need to update this test once we have a dependency written.
     */
    @Test
    @Transactional
    public void testPostDelete_inUse() throws Exception {
//        // Create some dummy formats
//        final SerializerFormat format = entityTestTools.createStrategy("Format 1");
//        final long formatId = format.getId();
//
//        // Create a Views that uses our format.
//        final View view1 = viewTestTools.createViewWithFormat("My Name", format);
//        final View view2 = viewTestTools.createViewWithFormat("My Other Name", format);
//
//        // Generate a dummy file
//        final Path expectedJarPath = Paths.get(entityUploadPath, format.getJar());
//        FileTestTools.createDummyFile(expectedJarPath.toString(), "MyContests");
//        assertTrue("Sanity test", Files.exists(expectedJarPath));
//
//        final String expectedError = "partitioning strategy in use by views: [My Name, My Other Name]";
//
//        // Hit index.
//        final MvcResult result = mockMvc
//            .perform(post(controllerPath + "/delete/" + formatId)
//                .with(user(adminUserDetails))
//                .with(csrf()))
//            .andDo(print())
//            .andExpect(status().is3xxRedirection())
//            .andExpect(redirectedUrl(controllerPath))
//            .andReturn();
//
//        final FlashMessage flashMessage = (FlashMessage) result.getFlashMap().get("FlashMessage");
//        assertNotNull("Should have a flash message set", flashMessage);
//        assertTrue("Should be warning", flashMessage.isWarning());
//        assertEquals("Should have our error msg", expectedError, flashMessage.getMessage());
//
//        // Validate
//        final SerializerFormat messageFormat = entityRepository.findById(formatId).get();
//        assertNotNull("Should NOT have removed partitioning strategy", messageFormat);
//
//        // Jar should still exist
//        assertTrue("Jar should not have been removed", Files.exists(expectedJarPath));
//
//        // Cleanup
//        Files.deleteIfExists(expectedJarPath);
    }
}