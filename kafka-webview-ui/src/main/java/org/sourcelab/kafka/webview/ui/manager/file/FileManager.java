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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Manages loading files from FileStorageServices.  This class manages a local read/write thru cache of the files
 * on local disk so they can be loaded.
 *
 * In situations where local disk is always maintained (IE installed on dedicated hardware) then using the default
 * LocalFileStorageService should transparent. You upload a file, it gets saved to local disk, things just work.
 *
 * In situations where local disk may not always be maintained (IE deployed on a containerization/virtualization service
 * where disk space may be ethereal) this class attempts to provide the same mechnisms as if the files were stored on
 * local disk, even if behind the scenes it's retrieving from some external data storage service.
 */
public class FileManager {
    private static final Logger logger = LoggerFactory.getLogger(FileManager.class);
    private final FileStorageService fileStorageService;
    private final LocalDiskStorage localCacheStorage;

    public FileManager(final FileStorageService fileStorageService, final LocalDiskStorage localCacheStorage) {
        this.fileStorageService = Objects.requireNonNull(fileStorageService);
        this.localCacheStorage = Objects.requireNonNull(localCacheStorage);
    }

    public synchronized Path getFile(final String filename, final FileType fileType) throws IOException {
        // If the file is just stored locally,
        if (fileStorageService instanceof LocalFileStorageService) {
            // No need to deal with pull through cache. Just retrieve path directly.
            return ((LocalFileStorageService) fileStorageService).getLocalPathToFile(filename, fileType);
        }

        // If the file exists within our local disk cache
        if (localCacheStorage.doesFileExist(filename, fileType)) {
            // Return path to the file on local disk.
            localCacheStorage.getLocalPathToFile(filename, fileType);
        }

        // If not in the cache, we'll retrieve the file from the external source (S3, Database, etc..)
        // and save it to the local disk as a cache.
        final InputStream inputStream = fileStorageService.getFile(filename, fileType);

        // Save to local disk cache
        localCacheStorage.saveFile(inputStream, filename, fileType);

        // Return local path
        return localCacheStorage.getLocalPathToFile(filename, fileType);
    }

    public synchronized boolean deleteFile(final String filename, final FileType fileType) throws IOException {

        // Delete from local cache
        localCacheStorage.deleteFile(filename, fileType);

        // Delete from storage service.
        return fileStorageService.deleteFile(filename, fileType);
    }

    public synchronized boolean doesFileExist(final String filename, final FileType fileType) throws IOException {
        return fileStorageService.doesFileExist(filename, fileType);
    }

    public synchronized boolean putFile(final InputStream inputStream, final String filename, final FileType fileType) throws IOException {
        // If the storage engine is just a local file on disk.
        if (fileStorageService instanceof LocalFileStorageService) {
            // Just save it and we're done.
            return fileStorageService.saveFile(inputStream, filename, fileType);
        }

        // Otherwise remove from local cache
        if (localCacheStorage.doesFileExist(filename, fileType)) {
            localCacheStorage.deleteFile(filename, fileType);
        }

        // Write to local cache
        inputStream.reset();
        localCacheStorage.saveFile(inputStream, filename, fileType);

        // Write to storage service
        inputStream.reset();
        return fileStorageService.saveFile(inputStream, filename, fileType);
    }
}
