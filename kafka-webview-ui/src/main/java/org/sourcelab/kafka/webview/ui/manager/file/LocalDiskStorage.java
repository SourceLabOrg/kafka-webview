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
import java.util.Arrays;
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

        try {
            final Path rootPath = Paths.get(uploadPath).toAbsolutePath();
            if (!Files.isDirectory(rootPath)) {
                Files.createDirectory(rootPath);
            }

            // Ensure all of our directories exist
            for (final FileType fileType : FileType.values()) {
                final Path typeDirectory = getPathForType(fileType);
                if (Files.isDirectory(typeDirectory)) {
                    continue;
                }
                Files.createDirectory(typeDirectory);
            }
        } catch (final IOException exception) {
            throw new RuntimeException("Unable to create directory: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean saveFile(final InputStream fileInputStream, final String filename, final FileType type) throws IOException {
        final Path rootPath = getPathForType(type);
        if (!Files.exists(rootPath)) {
            Files.createDirectory(rootPath);
        }

        // Create final output file name
        final Path fullOutputPath = getFilePath(filename, type);
        if (fullOutputPath.toFile().exists()) {
            throw new IOException("Output file already exists with filename: " + fullOutputPath.toString());
        }

        // Get the file and save it somewhere
        Files.copy(fileInputStream, fullOutputPath);

        return true;
    }

    @Override
    public boolean doesFileExist(final String filename, final FileType type) throws IOException {
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
    public boolean moveFile(final String originalFilename, final String newFileName, final FileType type) throws IOException {
        if (!doesFileExist(originalFilename, type)) {
            throw new IOException("Unable to find original file name: " + originalFilename);
        }

        // Delete destination
        Files.deleteIfExists(getFilePath(newFileName, type));

        // Move original file to destination file
        Files.move(
            getFilePath(originalFilename, type),
            getFilePath(newFileName, type)
        );

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

    private Path getPathForType(final FileType type) {
        switch (type) {
            // For backwards compat.
            case DESERIALIZER:
                return Paths.get(uploadPath, "deserializers").toAbsolutePath();
            case FILTER:
                return Paths.get(uploadPath, "filters").toAbsolutePath();
            case KEYSTORE:
                return Paths.get(uploadPath, "keyStores").toAbsolutePath();

            // Any future ones just use the enum type.
            default:
                return Paths.get(uploadPath, type.name()).toAbsolutePath();
        }
    }

    private Path getFilePath(final String filename, final FileType type) {
        final Path rootPath = getPathForType(type);

        // Create final output file name
        return rootPath.resolve(filename).toAbsolutePath();
    }

    @Override
    public Path getLocalPathToFile(final String filename, final FileType fileType) {
        return getFilePath(filename, fileType);
    }
}
