package com.darksci.kafkaview.manager.plugin;

import com.darksci.kafkaview.manager.ui.FlashMessage;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

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
        final File parentDir = new File(deserializerUploadPath);
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + deserializerUploadPath);
        }

        // Create final output file name
        final Path fullOutputPath = Paths.get(deserializerUploadPath, outFileName);
        if (fullOutputPath.toFile().exists()) {
            throw new IOException("Output file already exists");
        }

        // Get the file and save it somewhere
        final byte[] bytes = file.getBytes();
        Files.write(fullOutputPath, bytes);

        return fullOutputPath.toString();
    }
}
