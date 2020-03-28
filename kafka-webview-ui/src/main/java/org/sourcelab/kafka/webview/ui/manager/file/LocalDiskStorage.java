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

package org.sourcelab.kafka.webview.ui.manager.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Implementation that stores files on local disk.
 */
public class LocalDiskStorage implements FileStorageService, LocalFileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(LocalDiskStorage.class);

    /**
     * Root upload path.
     */
    private final String uploadPath;

    /**
     * Constructor.
     * @param uploadPath Parent upload directory.
     */
    public LocalDiskStorage(final String uploadPath) {
        this.uploadPath = Objects.requireNonNull(uploadPath);
    }

    @Override
    public boolean saveFile(final InputStream fileInputStream, final String filename, final FileType type) throws IOException {
        final String rootPath = getPathForType(type);
        final File parentDir = new File(rootPath);
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to createConsumer directory: " + rootPath);
        }

        // Create final output file name
        final Path fullOutputPath = Paths.get(rootPath, filename);
        if (fullOutputPath.toFile().exists()) {
            throw new IOException("Output file already exists with filename: " + fullOutputPath.toString());
        }

        // Get the file and save it somewhere
        Files.copy(fileInputStream, fullOutputPath);

        return true;
    }

    @Override
    public boolean doesFileExist(final String filename, final FileType type) throws IOException {
        final String rootPath = getPathForType(type);
        final File parentDir = new File(rootPath);

        // If the parent dir doesn't exist
        if (!parentDir.exists()) {
            // The file can't exist... right?
            return false;
        }

        // Create final output file name
        final Path fullOutputPath = getFilePath(filename, type);
        return fullOutputPath.toFile().exists();
    }

    @Override
    public boolean deleteFile(final String filename, final FileType type) throws IOException {
        // Handle nulls gracefully.
        if (filename == null || filename.trim().isEmpty()) {
            return true;
        }

        // Create final output file name
        final Path fullOutputPath = getFilePath(filename, type);
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

    @Override
    public InputStream getFile(final String filename, final FileType type) throws IOException {
        if (!doesFileExist(filename, type)) {
            // error?
            throw new IOException("File does not exist at " + getFilePath(filename, type));
        }
        final Path fullOutputPath = getFilePath(filename, type);
        return Files.newInputStream(fullOutputPath);
    }

    private String getPathForType(final FileType type) {
        switch (type) {
            // For backwards compat.
            case DESERIALIZER:
                return uploadPath + "/deserializers";
            case FILTER:
                return uploadPath + "/filters";
            case KEYSTORE:
                return uploadPath + "/keyStores";

            // Any future ones just use the enum type.
            default:
                return uploadPath + "/" + type.name();
        }
    }

    private Path getFilePath(final String filename, final FileType type) {
        final String rootPath = getPathForType(type);

        // Create final output file name
        return Paths.get(rootPath, filename).toAbsolutePath();
    }

    @Override
    public Path getLocalPathToFile(final String filename, final FileType fileType) {
        return getFilePath(filename, fileType);
    }
}
