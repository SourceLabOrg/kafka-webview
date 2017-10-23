package com.darksci.kafka.webview.manager.plugin;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadManager {
    private final String deserializerUploadPath;
    private final String filterUploadPath;
    private final String keyStoreUploadPath;

    public UploadManager(final String uploadPath) {
        this.deserializerUploadPath = uploadPath + "/deserializers";
        this.filterUploadPath = uploadPath + "/filters";
        this.keyStoreUploadPath = uploadPath + "/keyStores";
    }

    public String getDeserializerUploadPath() {
        return deserializerUploadPath;
    }

    public String getFilterUploadPath() {
        return filterUploadPath;
    }

    private String getKeyStoreUploadPath() {
        return keyStoreUploadPath;
    }

    public String handleDeserializerUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleFileUpload(file, outFileName, deserializerUploadPath);
    }

    public String handleFilterUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleFileUpload(file, outFileName, filterUploadPath);
    }

    public String handleKeystoreUpload(final MultipartFile file, final String outFileName) throws IOException {
        return handleFileUpload(file, outFileName, keyStoreUploadPath);
    }

    public boolean deleteKeyStore(final String keyStoreFile) {
        return deleteFile(keyStoreFile, keyStoreUploadPath);
    }

    private boolean deleteFile(final String filename, final String rootPath) {
        // Create final output file name
        final Path fullOutputPath = Paths.get(rootPath, filename).toAbsolutePath();

        if (!fullOutputPath.toFile().exists()) {
            return true;
        }

        try {
            Files.delete(fullOutputPath);
        } catch (IOException ex) {
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
        final byte[] bytes = file.getBytes();
        Files.write(fullOutputPath, bytes);

        return fullOutputPath.toString();
    }
}
