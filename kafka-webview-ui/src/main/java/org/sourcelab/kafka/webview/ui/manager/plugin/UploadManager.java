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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles uploading jars from the frontend UI and placing them into the expected locations on disk.
 */
public class UploadManager {
    private static final Logger logger = LoggerFactory.getLogger(UploadManager.class);

    /**
     * Where to upload JARs associated with a deserializer.
     */
    private final String deserializerUploadPath;

    /**
     * Where to upload JARs associated with filters.
     */
    private final String filterUploadPath;

    /**
     * Where to upload SSL JKS key stores.
     */
    private final String keyStoreUploadPath;

    /**
     * Constructor.
     * @param uploadPath Parent upload directory.
     */
    public UploadManager(final String uploadPath) {
        this.deserializerUploadPath = uploadPath + "/deserializers";
        this.filterUploadPath = uploadPath + "/filters";
        this.keyStoreUploadPath = uploadPath + "/keyStores";
    }

    String getDeserializerUploadPath() {
        return deserializerUploadPath;
    }

    String getFilterUploadPath() {
        return filterUploadPath;
    }

    String getKeyStoreUploadPath() {
        return keyStoreUploadPath;
    }

    /**
     * Handle uploading a Deserializer Jar.
     * @param file The Uploaded MultiPart file.
     * @param outFileName What we want to name the output file.
     * @return Path to uploaded file.
     */
    public String handleDeserializerUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleFileUpload(file, outFileName, getDeserializerUploadPath());
    }

    /**
     * Handle uploading a Filter Jar.
     * @param file The Uploaded MultiPart file.
     * @param outFileName What we want to name the output file.
     * @return Path to uploaded file.
     */
    public String handleFilterUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleFileUpload(file, outFileName, getFilterUploadPath());
    }

    /**
     * Handle uploading a JKS Keystore.
     * @param file The Uploaded MultiPart file.
     * @param outFileName What we want to name the output file.
     * @return Path to uploaded file.
     */
    public String handleKeystoreUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleFileUpload(file, outFileName, getKeyStoreUploadPath());
    }

    /**
     * Enables the ability to delete a keystore file.
     * @param keyStoreFile Filename of keystore file to be removed.
     * @return True if successful, false if not.
     */
    public boolean deleteKeyStore(final String keyStoreFile) {
        return deleteFile(keyStoreFile, keyStoreUploadPath);
    }

    private boolean deleteFile(final String filename, final String rootPath) {
        // Handle nulls gracefully.
        if (filename == null || filename.trim().isEmpty()) {
            return true;
        }

        // Create final output file name
        final Path fullOutputPath = Paths.get(rootPath, filename).toAbsolutePath();

        if (!fullOutputPath.toFile().exists()) {
            return true;
        }

        // Only remove files
        if (!fullOutputPath.toFile().isFile()) {
            return false;
        }

        try {
            Files.delete(fullOutputPath);
        } catch (final IOException ex) {
            logger.error("Failed to remove file {} - {}", fullOutputPath, ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    private String handleFileUpload(final MultipartFile file, final String outFileName, final String rootPath) throws IOException {
        final File parentDir = new File(rootPath);
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to createConsumer directory: " + rootPath);
        }

        // Create final output file name
        final Path fullOutputPath = Paths.get(rootPath, outFileName);
        if (fullOutputPath.toFile().exists()) {
            throw new IOException("Output file already exists");
        }

        // Get the file and save it somewhere
        try (BufferedInputStream in = new BufferedInputStream(file.getInputStream())) {
            Files.copy(in, fullOutputPath);
        }

        return fullOutputPath.toString();
    }
}
