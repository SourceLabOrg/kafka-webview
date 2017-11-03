package com.darksci.kafka.webview.ui.manager.plugin;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

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

}