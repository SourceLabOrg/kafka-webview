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

package org.sourcelab.kafka.webview.ui.controller.configuration.filter;

import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.controller.AbstractMvcTest;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.manager.user.permission.Permissions;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.repository.FilterRepository;
import org.sourcelab.kafka.webview.ui.tools.FileTestTools;
import org.sourcelab.kafka.webview.ui.tools.FilterTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

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
public class FilterConfigControllerTest extends AbstractMvcTest {

    @Autowired
    private FilterTestTools filterTestTools;

    @Autowired
    private FilterRepository filterRepository;

    /**
     * Where deserializer files are uploaded to.
     */
    private String filterUploadPath;

    @Before
    public void setupUploadPath() {
        filterUploadPath = uploadPath + "/filters/";
    }

    /**
     * Ensure authentication is required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthentication() throws Exception {
        // Filter index page.
        testUrlRequiresAuthentication("/configuration/filter", false);

        // Filter create page.
        testUrlRequiresAuthentication("/configuration/filter/create", false);
        testUrlRequiresAuthentication("/configuration/filter/create", true);

        // Filter edit page.
        testUrlRequiresAuthentication("/configuration/filter/edit/1", false);
        testUrlRequiresAuthentication("/configuration/filter/update", true);

        // Filter delete page.
        testUrlRequiresAuthentication("/configuration/filter/delete/1", true);
    }

    /**
     * Ensure correct permissions are required.
     */
    @Test
    @Transactional
    public void testUrlsRequireAuthorization() throws Exception {
        // Create at least one filter.
        final Filter filter = filterTestTools.createFilter("Test Filter " + System.currentTimeMillis());

        // Filter index page.
        testUrlRequiresPermission("/configuration/filter", false, Permissions.VIEW_READ);

        // Filter create page.
        testUrlRequiresPermission("/configuration/filter/create", false, Permissions.FILTER_CREATE);
        testUrlRequiresPermission("/configuration/filter/create", true, Permissions.FILTER_CREATE);

        // Filter edit page.
        testUrlRequiresPermission("/configuration/filter/edit/" + filter.getId(), false, Permissions.FILTER_MODIFY);
        testUrlRequiresPermission("/configuration/filter/update", true, Permissions.FILTER_MODIFY);

        // Filter delete page.
        testUrlRequiresPermission("/configuration/filter/delete/1", true, Permissions.FILTER_DELETE);
    }

    /**
     * Smoke test the Filter Index page.
     */
    @Test
    @Transactional
    public void testGetIndex() throws Exception {
        final Permissions[] permissions = {
            Permissions.VIEW_READ,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create some dummy filters
        final Filter filter1 = filterTestTools.createFilter("Filter1");
        final Filter filter2 = filterTestTools.createFilter("Filter2");

        // Hit index.
        mockMvc
            .perform(get("/configuration/filter").with(user(user)))
            //.andDo(print())
            .andExpect(status().isOk())
            // Validate filter 1
            .andExpect(content().string(containsString(filter1.getName())))
            .andExpect(content().string(containsString(filter1.getClasspath())))

            // Validate filter 2
            .andExpect(content().string(containsString(filter2.getName())))
            .andExpect(content().string(containsString(filter2.getClasspath())));
    }

    /**
     * Smoke test the Filter create page.
     */
    @Test
    @Transactional
    public void testGetCreate() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_CREATE,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit index.
        mockMvc
            .perform(get("/configuration/filter/create")
                .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("New Filter")))
            // Should submit to the create end point
            .andExpect(content().string(containsString("action=\"/configuration/filter/create\"")))
            .andExpect(content().string(containsString("Submit")));
    }

    /**
     * Smoke test the Filter edit page.
     */
    @Test
    @Transactional
    public void testGetEdit() throws Exception {
        // Create a Filter.
        final Filter filter = filterTestTools.createFilter("Test Filter " + System.currentTimeMillis());

        final Permissions[] permissions = {
            Permissions.FILTER_MODIFY,
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Hit index.
        mockMvc
            .perform(get("/configuration/filter/edit/" + filter.getId())
                .with(user(user)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(filter.getName())))
            // Should submit to the update end point.
            .andExpect(content().string(containsString("action=\"/configuration/filter/update\"")))
            .andExpect(content().string(containsString(
                "<input type=\"hidden\" name=\"id\" id=\"id\" value=\"" + filter.getId() + "\">"
            )))
            .andExpect(content().string(containsString("Submit")));
    }

    /**
     * Test that you cannot update a filter by submitting a request to the create end point
     * with an id parameter.
     */
    @Test
    @Transactional
    public void testPostCreate_withId_shouldResultIn400Error() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_MODIFY,
            Permissions.FILTER_CREATE,
            Permissions.FILTER_DELETE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a Filter.
        final String expectedFilterName = "My New Filter Name " + System.currentTimeMillis();
        final Filter filter = filterTestTools.createFilter(expectedFilterName);

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/filter/create")
                .with(user(user))
                .with(csrf())
                // Post the ID parameter to attempt to update existing filter via create end point.
                .param("id", String.valueOf(filter.getId()))
                .param("name", "Updated Name"))
            .andExpect(status().is4xxClientError());

        // Lookup Filter
        final Filter updatedFilter = filterRepository.findById(filter.getId()).get();
        assertNotNull("Should have filter", updatedFilter);
        assertEquals("Has original name -- was not updated", expectedFilterName, filter.getName());
    }

    /**
     * Test that you cannot create a filter by submitting a request to the update end point
     * without an id parameter.
     */
    @Test
    @Transactional
    public void testPostUpdate_withOutId_shouldResultIn400Error() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_MODIFY,
            Permissions.FILTER_CREATE,
            Permissions.FILTER_DELETE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a Filter.
        final String expectedFilterName = "My New Filter Name " + System.currentTimeMillis();

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/filter/update")
                .with(user(user))
                .with(csrf())
                // Don't include the Id Parameter in this request
                .param("name", expectedFilterName))
            .andExpect(status().is4xxClientError());

        // Lookup Filter
        final Filter createdFilter = filterRepository.findByName(expectedFilterName);
        assertNull("Should not have filter", createdFilter);
    }

    /**
     * Test creating new filter.
     */
    @Test
    @Transactional
    public void testPostCreate_newFilter() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_CREATE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream("testDeserializer/testPlugins.jar");
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);


        final String expectedName = "My New Filter Name " + System.currentTimeMillis();
        final String expectedClassPath = "examples.filter.StringSearchFilter";

        // Hit Update end point.
        mockMvc
            .perform(multipart("/configuration/filter/create")
                .file(jarUpload)
                .with(user(user))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/filter"));

        // Lookup Filter
        final Filter filter = filterRepository.findByName(expectedName);
        assertNotNull("Should have new filter", filter);
        assertEquals("Has correct name", expectedName, filter.getName());
        assertEquals("Has correct classpath", expectedClassPath, filter.getClasspath());

        final boolean doesJarExist = Files.exists(Paths.get(filterUploadPath, filter.getJar()));
        assertTrue("Filter file should have been uploaded", doesJarExist);

        // Cleanup
        Files.deleteIfExists(Paths.get(filterUploadPath, filter.getJar()));
    }

    /**
     * Test attempting to create a new filter, but don't upload a jar.
     * This should be kicked back.
     */
    @Test
    @Transactional
    public void testPostCreate_createFilterMissingFile() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_CREATE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String expectedName = "My New Filter Name " + System.currentTimeMillis();
        final String expectedClassPath = "examples.filter.StringSearchFilter";

        final InputStream fileInputStream = null;
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/filter/create")
                .file(jarUpload)
                .with(user(user))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("filterForm", "file"))
            .andExpect(status().isOk());

        // Lookup Filter
        final Filter filter = filterRepository.findByName(expectedName);
        assertNull("Should NOT have filter", filter);
    }

    /**
     * Test attempting to create a new filter, but fail to load the filter class from the jar.
     */
    @Test
    @Transactional
    public void testPostCreate_createNewFilterInvalidJar() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_CREATE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String expectedName = "My New Filter Name " + System.currentTimeMillis();
        final String expectedClassPath = "examples.filter.StringSearchFilter";

        // This isn't a real jar, will fail validation.
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "contents".getBytes(Charsets.UTF_8));

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/filter/create")
                .file(jarUpload)
                .with(user(user))
                .with(csrf())
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("filterForm", "file"))
            .andExpect(status().isOk());

        // Lookup Filter
        final Filter filter = filterRepository.findByName(expectedName);
        assertNull("Should NOT have filter", filter);
    }

    /**
     * Test attempting to update an existing filter, but send over a bad Id.
     * This should be kicked back as an error.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingButWithBadMessageFormatId() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_MODIFY
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final String expectedName = "My New Filter Name " + System.currentTimeMillis();
        final String expectedClassPath = "examples.filter.StringSearchFilter";

        final MockMultipartFile jarUpload =
            new MockMultipartFile("file", "testPlugins.jar", null, "MyContent".getBytes(Charsets.UTF_8));

        // Hit page.
        final MvcResult result = mockMvc
            .perform(multipart("/configuration/filter/update")
                .file(jarUpload)
                .with(user(user))
                .with(csrf())
                .param("id", "-1000")
                .param("name", expectedName)
                .param("classpath", expectedClassPath))
            .andExpect(flash().attributeExists("FlashMessage"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/filter"))
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
        final Permissions[] permissions = {
            Permissions.FILTER_MODIFY
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final Filter filter = filterTestTools.createFilter("My Filter" + System.currentTimeMillis());
        final String expectedName = filter.getName();
        final String expectedClasspath = filter.getClasspath();
        final String expectedJarName = filter.getJar();
        final String expectedJarContents = "OriginalContents";
        final Path expectedJarPath = Paths.get(filterUploadPath, filter.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(filterUploadPath + filter.getJar(), expectedJarContents);

        // This is an invalid jar.
        final MockMultipartFile jarUpload =
            new MockMultipartFile("file", "testPlugins.jar", null, "MyContent".getBytes(Charsets.UTF_8));

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/filter/update")
                .file(jarUpload)
                .with(user(user))
                .with(csrf())
                .param("id", String.valueOf(filter.getId()))
                .param("name", "Updated Name")
                .param("classpath", "made.up.classpath"))
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("filterForm", "file"))
            .andExpect(status().isOk());

        // Validate filter was not updated.
        final Filter updatedFilter = filterRepository.findById(filter.getId()).get();
        assertNotNull("Has filter", updatedFilter);
        assertEquals("Name not updated", expectedName, updatedFilter.getName());
        assertEquals("classpath not updated", expectedClasspath, updatedFilter.getClasspath());
        assertEquals("Jar name not updated", expectedJarName, updatedFilter.getJar());

        // Validate previous jar not overwritten.
        assertTrue("File should exist", Files.exists(expectedJarPath));
        final String jarContents = FileTestTools.readFile(expectedJarPath.toString());
        assertEquals("Jar contents should not have changed", expectedJarContents, jarContents);

        // Cleanup
        Files.deleteIfExists(expectedJarPath);
    }

    /**
     * Test attempting to update an existing filter, but not providing a jar.
     * We should change the name, but not touch the jar/classpath at all.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingNoJarUploaded() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_MODIFY
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final Filter filter = filterTestTools.createFilter("My Filter" + System.currentTimeMillis());
        final String expectedName = filter.getName();
        final String expectedClasspath = filter.getClasspath();
        final String expectedJarName = filter.getJar();
        final String expectedJarContents = "OriginalContents";
        final Path expectedJarPath = Paths.get(filterUploadPath, filter.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(filterUploadPath + filter.getJar(), expectedJarContents);

        // This is the same as not uploading a jar.
        final InputStream emptyFile = null;
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, emptyFile);

        final String newName = "MyUpdatedName" + System.currentTimeMillis();
        final String newClasspath = "my new class path";

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/filter/update")
                .file(jarUpload)
                .with(user(user))
                .with(csrf())
                .param("id", String.valueOf(filter.getId()))
                .param("name", newName)
                .param("classpath", newClasspath))
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/filter"));

        // Validate filter was updated.
        final Filter updatedFilter = filterRepository.findById(filter.getId()).get();
        assertNotNull("Has filter", updatedFilter);
        assertEquals("Name updated", newName, updatedFilter.getName());
        assertEquals("classpath was NOT updated", expectedClasspath, filter.getClasspath());
        assertNotEquals("classpath was NOT updated", newClasspath, filter.getClasspath());
        assertEquals("Jar name not updated", expectedJarName, updatedFilter.getJar());

        // Validate jar should still exist
        assertTrue("File should exist", Files.exists(expectedJarPath));

        // Jar contents should be unchanged
        final String jarContents = FileTestTools.readFile(expectedJarPath.toString());
        assertEquals("Jar contents should not have changed", expectedJarContents, jarContents);

        // Cleanup
        Files.deleteIfExists(expectedJarPath);
    }

    /**
     * Test attempting to update an existing filter, uploading a valid jar.
     * We change the name.  We expect the old file to be removed, and the new one added.
     */
    @Test
    @Transactional
    public void testPostUpdate_updatingExistingWithValidJarSameName() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_MODIFY
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        final Filter filter = filterTestTools.createFilter("My Filter" + System.currentTimeMillis());
        final String expectedName = filter.getName();
        final String expectedClasspath = filter.getClasspath();
        final String originalJarName = filter.getJar();
        final String originalJarContents = "OriginalContents";
        final Path originalJarPath = Paths.get(filterUploadPath, filter.getJar());

        // Create a dummy jar
        FileTestTools.createDummyFile(filterUploadPath + filter.getJar(), originalJarContents);

        // This is a valid jar
        final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream("testDeserializer/testPlugins.jar");
        final MockMultipartFile jarUpload = new MockMultipartFile("file", "testPlugins.jar", null, fileInputStream);

        final String newName = "MyUpdatedName" + System.currentTimeMillis();
        final String newClasspath = "examples.filter.StringSearchFilter";

        // Hit page.
        mockMvc
            .perform(multipart("/configuration/filter/update")
                .file(jarUpload)
                .with(user(user))
                .with(csrf())
                .param("id", String.valueOf(filter.getId()))
                .param("name", newName)
                .param("classpath", newClasspath))
            .andExpect(model().hasNoErrors())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/filter"));

        // Validate filter was updated.
        final Filter updatedFilter = filterRepository.findById(filter.getId()).get();
        assertNotNull("Has message format", updatedFilter);
        assertEquals("Name updated", newName, updatedFilter.getName());
        assertEquals("classpath updated", newClasspath, updatedFilter.getClasspath());
        assertNotEquals("Jar name not updated", originalJarName, updatedFilter.getJar());

        // Validate previous jar is gone/deleted.
        assertFalse("File should NOT exist", Files.exists(originalJarPath));

        // Validate new jar is created.
        final Path newJarPath = Paths.get(filterUploadPath, updatedFilter.getJar());
        assertTrue("New jar should exist", Files.exists(newJarPath));

        // Cleanup
        Files.deleteIfExists(newJarPath);
    }

    /**
     * Test deleting filter.
     */
    @Test
    @Transactional
    public void testPostDelete() throws Exception {
        final Permissions[] permissions = {
            Permissions.FILTER_DELETE
        };
        final UserDetails user = userTestTools.createUserDetailsWithPermissions(permissions);

        // Create a Filter.
        final String expectedFilterName = "My New Filter Name " + System.currentTimeMillis();
        final Filter filter = filterTestTools.createFilter(expectedFilterName);

        // Hit Update end point.
        mockMvc
            .perform(post("/configuration/filter/delete/" + filter.getId())
                .with(user(user))
                .with(csrf())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/configuration/filter"));

        // Lookup Cluster
        final Optional<Filter> filterOptional = filterRepository.findById(filter.getId());
        assertFalse("Should no longer have filter", filterOptional.isPresent());
    }
}