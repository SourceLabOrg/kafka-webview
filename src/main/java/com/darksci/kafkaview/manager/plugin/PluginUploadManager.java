package com.darksci.kafkaview.manager.plugin;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PluginUploadManager {
    private final String deserializerUploadPath;
    private final String filterUploadPath;

    public PluginUploadManager(final String jarUploadPath) {
        this.deserializerUploadPath = jarUploadPath + "/deserializers";
        this.filterUploadPath = jarUploadPath + "/filters";
    }

    public String getDeserializerUploadPath() {
        return deserializerUploadPath;
    }

    public String getFilterUploadPath() {
        return filterUploadPath;
    }

    public String handleDeserializerUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleJarUpload(file, outFileName, deserializerUploadPath);
    }

    public String handleFilterUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleJarUpload(file, outFileName, filterUploadPath);
    }

    private String handleJarUpload(final MultipartFile file, final String outFileName, final String rootPath) throws IOException {
        final File parentDir = new File(rootPath);
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + rootPath);
        }

        // Create final output file name
        final Path fullOutputPath = Paths.get(rootPath, outFileName);
        if (fullOutputPath.toFile().exists()) {
            throw new IOException("Output file already exists");
        }

        // Get the file and save it somewhere
        final byte[] bytes = file.getBytes();
        Files.write(fullOutputPath, bytes);

        return fullOutputPath.toString();
    }
}
