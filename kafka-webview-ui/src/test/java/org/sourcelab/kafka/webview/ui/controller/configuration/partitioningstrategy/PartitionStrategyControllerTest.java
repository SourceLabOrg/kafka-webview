/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.controller.configuration.partitioningstrategy;

import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.PartitioningStrategy;
import org.sourcelab.kafka.webview.ui.repository.PartitioningStrategyRepository;
import org.sourcelab.kafka.webview.ui.tools.FileTestTools;
import org.sourcelab.kafka.webview.ui.tools.PartitioningStrategyTestTools;
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
public class PartitionStrategyControllerTest extends AbstractMvcTest {

    @Autowired
    private PartitioningStrategyTestTools partitioningStrategyTestTools;

    @Autowired
    private PartitioningStrategyRepository partitioningStrategyRepository;

    /**
     * Where Partitioners files are uploaded to.
     */
    private String partitionerUploadPath;

    @Before
    public void setupUploadPath() {
        partitionerUploadPath = uploadPath + "/partitioners/";
    }

    /**
     * Test cannot load pages w/o admin role.
     */
    @Test
    @Transactional
    public void test_withoutAdminRole() throws Exception {
        testUrlWithOutAdminRole("/configuration/partitionStrategy", false);
        testUrlWithOutAdminRole("/configuration/partitionStrategy/create", false);
        testUrlWithOutAdminRole("/configuration/partitionStrategy/edit/1", false);
        testUrlWithOutAdminRole("/configuration/partitionStrategy/create", true);
        testUrlWithOutAdminRole("/configuration/partitionStrategy/delete/1", true);
    }

    /**
     * Smoke test the partitioning strategy Index page.
     */
    @Test
    @Transactional
    public void testIndex() throws Exception {
        // Create some custom formats
        final PartitioningStrategy partitioner1 = partitioningStrategyTestTools.createStrategy("Strategy 1");
        final PartitioningStrategy partitioner2 = partitioningStrategyTestTools.createStrategy("Strategy 2");

        // Check for default instance
        final String defaultInstanceName = "Default Partitioner";
        final String defaultInstanceClass = "org.apache.kafka.clients.producer.internals.DefaultPartitioner";

        // Hit index.
        mockMvc
            .perform(get("/configuration/partitionStrategy").with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            // Validate instance 1
            .andExpect(content().string(containsString(partitioner1.getName())))
            .andExpect(content().string(containsString(partitioner1.getClasspath())))

            // Validate instance 2
            .andExpect(content().string(containsString(partitioner2.getName())))
            .andExpect(content().string(containsString(partitioner2.getClasspath())))

            // Validate default instance
            .andExpect(content().string(containsString(defaultInstanceName)))
            .andExpect(content().string(containsString(defaultInstanceClass)));
    }

    /**
     * Smoke test the partitioning strategy create form.
     */
    @Test
    @Transactional
    public void testGetCreate() throws Exception {
        // Hit index.
        mockMvc
            .perform(get("/configuration/partitionStrategy/create").with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    /**
     * Smoke test creating new partitioning strategy.
     */
    @Test
    @Transactional
    public void testPostUpdate_newPartitioningStrategy() throws Exception {
        final String expectedName = "MyPartitioner" + System.currentTimeMillis();
        final String expectedClassPath = "examples.partitioner.StaticPartitioner";

        final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream("testDeserializer/testPlugins.jar");
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);

        // Define our expected json string
        final String expectedOptionsJson = "{\"option1\":\"value1\",\"option2\":\"value2\"}";

        // Hit index.
        mockMvc
            .perform(multipart("/configuration/partitionStrategy/update")
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
            .andExpect(redirectedUrl("/configuration/partitionStrategy"));

        // Validate
        final PartitioningStrategy partitioningStrategy = partitioningStrategyRepository.findByName(expectedName);
        assertNotNull("Should have partitioning strategy", partitioningStrategy);
        assertEquals("Has correct name", expectedName, partitioningStrategy.getName());
        assertEquals("Has correct classpath", expectedClassPath, partitioningStrategy.getClasspath());
        assertNotNull("Has jar path", partitioningStrategy.getJar());
        assertFalse("Should not be a default format", partitioningStrategy.isDefault());

        // Validate that our options got set
        assertEquals("Got options", expectedOptionsJson, partitioningStrategy.getOptionParameters());

        final boolean doesJarExist = Files.exists(Paths.get(partitionerUploadPath, partitioningStrategy.getJar()));
        assertTrue("Partitioner jar file should have been uploaded", doesJarExist);

        // Cleanup
        Files.deleteIfExists(Paths.get(partitionerUploadPath, partitioningStrategy.getJar()));
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
            .perform(multipart("/configuration/partitionStrategy/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            .andDo(print())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("partitioningStrategyForm", "file"))
            .andExpect(status().isOk());

        // Validate
        final PartitioningStrategy messageFormat = partitioningStrategyRepository.findByName(expectedName);
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
            .perform(multipart("/configuration/partitionStrategy/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            .andDo(print())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("partitioningStrategyForm", "file"))
            .andExpect(status().isOk());

        // Validate
        final PartitioningStrategy messageFormat = partitioningStrategyRepository.findByName(expectedName);
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
            .perform(multipart("/configuration/partitionStrategy/update")
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
        final PartitioningStrategy partitioningStrategy = partitioningStrategyTestTools.createStrategy("MyPartitioner" + System.currentTimeMillis());
        final String expectedName = partitioningStrategy.getName();
        final String expectedClasspath = partitioningStrategy.getClasspath();
        final String expectedJarName = partitioningStrategy.getJar();
        final String expectedJarContents = "OriginalContents";
        final String expectedParametersJson = partitioningStrategy.getOptionParameters();
        final Path expectedJarPath = Paths.get(partitionerUploadPath, partitioningStrategy.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(partitionerUploadPath + partitioningStrategy.getJar(), expectedJarContents);

        // This is an invalid jar.
        final MockMultipartFile jarUpload =
            new MockMultipartFile("file", "testPlugins.jar", null, "MyContent".getBytes(Charsets.UTF_8));

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/partitionStrategy/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(partitioningStrategy.getId()))
                .param("name", "Updated Name")
                .param("classpath", "made.up.classpath")
                .param("customOptionNames", "option1")
                .param("customOptionNames", "option2")
                .param("customOptionValues", "value1")
                .param("customOptionValues", "value2"))
            .andDo(print())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("partitioningStrategyForm", "file"))
            .andExpect(status().isOk());

        // Validate partitioning strategy was not updated.
        final PartitioningStrategy updatedPartitioningStrategy = partitioningStrategyRepository.findById(partitioningStrategy.getId()).get();
        assertNotNull("Has partitioning strategy", updatedPartitioningStrategy);
        assertEquals("Name not updated", expectedName, updatedPartitioningStrategy.getName());
        assertEquals("classpath not updated", expectedClasspath, updatedPartitioningStrategy.getClasspath());
        assertEquals("Jar name not updated", expectedJarName, updatedPartitioningStrategy.getJar());
        assertFalse("Should not be a default format", updatedPartitioningStrategy.isDefault());
        assertEquals("Parameters not updated", expectedParametersJson, updatedPartitioningStrategy.getOptionParameters());

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
        final PartitioningStrategy partitioningStrategy = partitioningStrategyTestTools.createStrategy("myPatitioner" + System.currentTimeMillis());
        final String originalClasspath = partitioningStrategy.getClasspath();
        final String originalJarName = partitioningStrategy.getJar();
        final String originalJarContents = "OriginalContents";
        final Path originalJarPath = Paths.get(partitionerUploadPath, partitioningStrategy.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(partitionerUploadPath + partitioningStrategy.getJar(), originalJarContents);

        // This is the same as not uploading a jar.
        final InputStream emptyFile = null;
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, emptyFile);

        final String newName = "MyUpdatedName" + System.currentTimeMillis();
        final String newClasspath = "my new class path";

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/partitionStrategy/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(partitioningStrategy.getId()))
                .param("name", newName)
                .param("classpath", newClasspath)
                .param("customOptionNames", "option1")
                .param("customOptionNames", "option2")
                .param("customOptionValues", "value1")
                .param("customOptionValues", "value2"))
            .andDo(print())
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/partitionStrategy"));

        // Validate partitioning strategy was updated.
        final PartitioningStrategy updatedPartitioningStrategy = partitioningStrategyRepository.findById(partitioningStrategy.getId()).get();
        assertNotNull("Has partitioning strategy", updatedPartitioningStrategy);
        assertEquals("Name updated", newName, updatedPartitioningStrategy.getName());
        assertEquals("classpath was NOT updated", originalClasspath, updatedPartitioningStrategy.getClasspath());
        assertNotEquals("classpath was NOT updated", newClasspath, updatedPartitioningStrategy.getClasspath());
        assertEquals("Jar name not updated", originalJarName, updatedPartitioningStrategy.getJar());
        assertFalse("Should not be a default format", updatedPartitioningStrategy.isDefault());

        // Define our expected json string
        final String expectedOptionsJson = "{\"option1\":\"value1\",\"option2\":\"value2\"}";
        assertEquals("Parameters should have been updated", expectedOptionsJson, updatedPartitioningStrategy.getOptionParameters());

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
        final PartitioningStrategy partitioningStrategy = partitioningStrategyTestTools.createStrategy("MyPartitioner" + System.currentTimeMillis());
        final String originalJarName = partitioningStrategy.getJar();
        final String originalJarContents = "OriginalContents";
        final Path originalJarPath = Paths.get(partitionerUploadPath, partitioningStrategy.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(partitionerUploadPath + partitioningStrategy.getJar(), originalJarContents);

        // This is a valid jar
        final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream("testDeserializer/testPlugins.jar");
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);

        final String newName = "MyUpdatedName" + System.currentTimeMillis();
        final String newClasspath = "examples.partitioner.StaticPartitioner";

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/partitionStrategy/update")
                .file(jarUpload)
                .with(user(adminUserDetails))
                .with(csrf())
                .param("id", String.valueOf(partitioningStrategy.getId()))
                .param("name", newName)
                .param("classpath", newClasspath))
            .andDo(print())
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/partitionStrategy"));

        // Validate partitioning strategy was updated.
        final PartitioningStrategy updatedPartitionStrategy = partitioningStrategyRepository.findById(partitioningStrategy.getId()).get();
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
        final Path newJarPath = Paths.get(partitionerUploadPath, updatedPartitionStrategy.getJar());
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
        final PartitioningStrategy partitioningStrategy = partitioningStrategyTestTools.createStrategy("My Partitioner" + System.currentTimeMillis());
        partitioningStrategy.setOptionParameters("{\"myOption1\":\"myValue1\",\"myOption2\":\"myValue2\"}");
        partitioningStrategyRepository.save(partitioningStrategy);

        // Hit edit page.
        mockMvc
            .perform(get("/configuration/partitionStrategy/edit/" + partitioningStrategy.getId())
                .with(user(adminUserDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(partitioningStrategy.getName())))
            .andExpect(content().string(containsString(partitioningStrategy.getClasspath())))

            // Should have our Id input
            .andExpect(content().string(containsString("value=\"" + partitioningStrategy.getId() + "\"")))

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
        final PartitioningStrategy partitioningStrategy = partitioningStrategyTestTools.createStrategy("Strategy 2");
        final long formatId = partitioningStrategy.getId();

        // Generate a dummy file
        final Path expectedJarPath = Paths.get(partitionerUploadPath, partitioningStrategy.getJar());
        FileTestTools.createDummyFile(expectedJarPath.toString(), "MyContents");
        assertTrue("Sanity test", Files.exists(expectedJarPath));

        // Hit index.
        mockMvc
            .perform(post("/configuration/partitionStrategy/delete/" + formatId)
                .with(user(adminUserDetails))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/partitionStrategy"));

        // Validate
        assertFalse("Should NOT have partitioning strategy", partitioningStrategyRepository.existsById(formatId));

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
//        final PartitioningStrategy format = partitioningStrategyTestTools.createStrategy("Format 1");
//        final long formatId = format.getId();
//
//        // Create a Views that uses our format.
//        final View view1 = viewTestTools.createViewWithFormat("My Name", format);
//        final View view2 = viewTestTools.createViewWithFormat("My Other Name", format);
//
//        // Generate a dummy file
//        final Path expectedJarPath = Paths.get(partitionerUploadPath, format.getJar());
//        FileTestTools.createDummyFile(expectedJarPath.toString(), "MyContests");
//        assertTrue("Sanity test", Files.exists(expectedJarPath));
//
//        final String expectedError = "partitioning strategy in use by views: [My Name, My Other Name]";
//
//        // Hit index.
//        final MvcResult result = mockMvc
//            .perform(post("/configuration/partitioningStrategy/delete/" + formatId)
//                .with(user(adminUserDetails))
//                .with(csrf()))
//            .andDo(print())
//            .andExpect(status().is3xxRedirection())
//            .andExpect(redirectedUrl("/configuration/messageFormat"))
//            .andReturn();
//
//        final FlashMessage flashMessage = (FlashMessage) result.getFlashMap().get("FlashMessage");
//        assertNotNull("Should have a flash message set", flashMessage);
//        assertTrue("Should be warning", flashMessage.isWarning());
//        assertEquals("Should have our error msg", expectedError, flashMessage.getMessage());
//
//        // Validate
//        final PartitioningStrategy messageFormat = partitioningStrategyRepository.findById(formatId).get();
//        assertNotNull("Should NOT have removed partitioning strategy", messageFormat);
//
//        // Jar should still exist
//        assertTrue("Jar should not have been removed", Files.exists(expectedJarPath));
//
//        // Cleanup
//        Files.deleteIfExists(expectedJarPath);
    }
}