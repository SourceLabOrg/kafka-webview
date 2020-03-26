package org.sourcelab.kafka.webview.ui.manager.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
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
public class LocalDiskStorage implements FileStorageService {
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
        final String rootPath = getPathFromType(type);
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
        final String rootPath = getPathFromType(type);
        final File parentDir = new File(rootPath);

        // If the parent dir doesn't exist
        if (!parentDir.exists()) {
            // The file can't exist... right?
            return false;
        }

        // Create final output file name
        final Path fullOutputPath = Paths.get(rootPath, filename);
        return fullOutputPath.toFile().exists();
    }

    @Override
    public boolean deleteFile(final String filename, final FileType type) throws IOException {
        // Handle nulls gracefully.
        if (filename == null || filename.trim().isEmpty()) {
            return true;
        }

        final String rootPath = getPathFromType(type);

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

    @Override
    public byte[] getFile(final String filename, final FileType type) throws IOException {
        // TODO
        return new byte[0];
    }

    private String getPathFromType(final FileType type) {
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
}
