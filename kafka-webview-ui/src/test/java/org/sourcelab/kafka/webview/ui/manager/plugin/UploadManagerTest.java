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

package org.sourcelab.kafka.webview.ui.manager.plugin;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UploadManagerTest {

    /**
     * Test constructor works about as we expect.
     */
    @Test
    public void testConstructor() {
        final String parentUploadDir = "/tmp/uploads";
        final String expectedDeserializerPath = parentUploadDir + "/deserializers";
        final String expectedFilterPath = parentUploadDir + "/filters";
        final String expectedKeyStorePath = parentUploadDir + "/keyStores";

        // Create manager
        final UploadManager uploadManager = new UploadManager(parentUploadDir);

        // Validate
        assertEquals("Has expected deserializer path", expectedDeserializerPath, uploadManager.getDeserializerUploadPath());
        assertEquals("Has expected filter path", expectedFilterPath, uploadManager.getFilterUploadPath());
        assertEquals("Has expected keystore path", expectedKeyStorePath, uploadManager.getKeyStoreUploadPath());
    }

    /**
     * Tests uploading a Deserializer file.
     */
    @Test
    public void testHandleDeserializerUpload() throws IOException {
        // Make a temp directory
        final Path tempDirectory = Files.createTempDirectory(null);

        // Create a "multi-part" file
        final String mockContent = "test content";
        final MockMultipartFile myFile = new MockMultipartFile(
            "data",
            "filename.txt",
            "text/plain",
            mockContent.getBytes(StandardCharsets.UTF_8)
        );

        final String outputFilename = "MyUpload.jar";
        final String expectedUploadedPath = tempDirectory.toString() + "/deserializers/" + outputFilename;

        // Create manager
        final UploadManager uploadManager = new UploadManager(tempDirectory.toString());

        // Handle the "upload"
        final String result = uploadManager.handleDeserializerUpload(myFile, outputFilename);

        // Validate
        assertEquals("Has expected result filename", expectedUploadedPath, result);

        // Validate contents
        final byte[] contentBytes = Files.readAllBytes(new File(result).toPath());
        final String contentString = new String(contentBytes, StandardCharsets.UTF_8);
        assertEquals("Contents are expected", mockContent, contentString);
    }

    /**
     * Tests uploading a Filter file.
     */
    @Test
    public void testHandleFilterUpload() throws IOException {
        // Make a temp directory
        final Path tempDirectory = Files.createTempDirectory(null);

        // Create a "multi-part" file
        final String mockContent = "test content";
        final MockMultipartFile myFile = new MockMultipartFile(
            "data",
            "filename.txt",
            "text/plain",
            mockContent.getBytes(StandardCharsets.UTF_8)
        );

        final String outputFilename = "MyUpload.jar";
        final String expectedUploadedPath = tempDirectory.toString() + "/filters/" + outputFilename;

        // Create manager
        final UploadManager uploadManager = new UploadManager(tempDirectory.toString());

        // Handle the "upload"
        final String result = uploadManager.handleFilterUpload(myFile, outputFilename);

        // Validate
        assertEquals("Has expected result filename", expectedUploadedPath, result);

        // Validate contents
        final byte[] contentBytes = Files.readAllBytes(new File(result).toPath());
        final String contentString = new String(contentBytes, StandardCharsets.UTF_8);
        assertEquals("Contents are expected", mockContent, contentString);
    }

    /**
     * Tests uploading a Deserializer file.
     */
    @Test
    public void testHandleKeyStoreUpload() throws IOException {
        // Make a temp directory
        final Path tempDirectory = Files.createTempDirectory(null);

        // Create a "multi-part" file
        final String mockContent = "test content";
        final MockMultipartFile myFile = new MockMultipartFile(
            "data",
            "filename.txt",
            "text/plain",
            mockContent.getBytes(StandardCharsets.UTF_8)
        );

        final String outputFilename = "MyUpload.jar";
        final String expectedUploadedPath = tempDirectory.toString() + "/keyStores/" + outputFilename;

        // Create manager
        final UploadManager uploadManager = new UploadManager(tempDirectory.toString());

        // Handle the "upload"
        final String result = uploadManager.handleKeystoreUpload(myFile, outputFilename);

        // Validate
        assertEquals("Has expected result filename", expectedUploadedPath, result);

        // Validate contents
        final Path filePath = new File(result).toPath();
        final byte[] contentBytes = Files.readAllBytes(filePath);
        final String contentString = new String(contentBytes, StandardCharsets.UTF_8);
        assertEquals("Contents are expected", mockContent, contentString);

        // Now test deleting a keystore
        final boolean deleteResult = uploadManager.deleteKeyStore(outputFilename);
        assertEquals("Should be true", true, deleteResult);
        assertFalse("File no longer exists", Files.exists(filePath));
    }

    /**
     * Test UploadManager gracefully handles deleting files that don't exist.
     */
    @Test
    public void testDeleteNonExistantFile() throws IOException {
        // Make a temp directory
        final Path tempDirectory = Files.createTempDirectory(null);

        // Create manager
        final UploadManager uploadManager = new UploadManager(tempDirectory.toString());

        final boolean result = uploadManager.deleteKeyStore("This-File-Does-not-exist");
        assertTrue("Gracefully returns true", result);
    }

    /**
     * Test UploadManager gracefully handles deleting empty string filenames that don't exist.
     */
    @Test
    public void testDeleteEmptyFile() throws IOException {
        // Make a temp directory
        final Path tempDirectory = Files.createTempDirectory(null);

        // Create manager
        final UploadManager uploadManager = new UploadManager(tempDirectory.toString());

        final boolean result = uploadManager.deleteKeyStore("");
        assertTrue("Gracefully returns true", result);

        // Sanity test
        assertTrue("Temp dir still exists", tempDirectory.toFile().exists());
    }

    /**
     * Test UploadManager gracefully handles deleting null string filenames.
     */
    @Test
    public void testDeleteNullFile() throws IOException {
        // Make a temp directory
        final Path tempDirectory = Files.createTempDirectory(null);

        // Create manager
        final UploadManager uploadManager = new UploadManager(tempDirectory.toString());

        final boolean result = uploadManager.deleteKeyStore(null);
        assertTrue("Gracefully returns true", result);

        // Sanity test
        assertTrue("Temp dir still exists", tempDirectory.toFile().exists());
    }
}