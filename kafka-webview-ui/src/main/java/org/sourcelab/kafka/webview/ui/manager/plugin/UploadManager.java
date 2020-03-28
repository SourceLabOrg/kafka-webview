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

import org.sourcelab.kafka.webview.ui.manager.file.FileManager;
import org.sourcelab.kafka.webview.ui.manager.file.FileStorageService;
import org.sourcelab.kafka.webview.ui.manager.file.FileType;
import org.sourcelab.kafka.webview.ui.manager.file.LocalDiskStorage;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Handles uploading jars from the frontend UI and placing them into the expected locations on disk.
 */
public class UploadManager {
    /**
     * Underlying Storage Mechanism.
     */
    private final FileManager fileManager;

    /**
     * Constructor.
     * @param fileManager manages file storage.
     */
    public UploadManager(final FileManager fileManager) {
        this.fileManager = Objects.requireNonNull(fileManager);
    }

    /**
     * Handle uploading a Deserializer Jar.
     * @param file The Uploaded MultiPart file.
     * @param outFileName What we want to name the output file.
     * @return Path to uploaded file.
     */
    public String handleDeserializerUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleFileUpload(file, outFileName, FileType.DESERIALIZER);
    }

    /**
     * Handle uploading a Filter Jar.
     * @param file The Uploaded MultiPart file.
     * @param outFileName What we want to name the output file.
     * @return Path to uploaded file.
     */
    public String handleFilterUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleFileUpload(file, outFileName, FileType.FILTER);
    }

    /**
     * Handle uploading a JKS Keystore.
     * @param file The Uploaded MultiPart file.
     * @param outFileName What we want to name the output file.
     * @return Path to uploaded file.
     */
    public String handleKeystoreUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleFileUpload(file, outFileName, FileType.KEYSTORE);
    }

    /**
     * Enables the ability to delete a keystore file.
     * @param keyStoreFile Filename of keystore file to be removed.
     * @return True if successful, false if not.
     */
    public boolean deleteKeyStore(final String keyStoreFile) throws IOException {
        return fileManager.deleteFile(keyStoreFile, FileType.KEYSTORE);
    }


    private String handleFileUpload(final MultipartFile file, final String outFileName, final FileType fileType) throws IOException {
        // Check if file exists
        if (fileManager.doesFileExist(outFileName, fileType)) {
            throw new IOException("Output file of type " + fileType.name() + " already exists with name " + outFileName);
        }

        // Store it
        fileManager.putFile(file.getInputStream(), outFileName, fileType);
        return outFileName;
    }
}
