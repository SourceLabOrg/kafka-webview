package org.sourcelab.kafka.webview.ui.manager.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines interface for storing files.
 */
public interface FileStorageService {

    boolean saveFile(final InputStream fileInputStream, final String filename, final FileType type) throws IOException;

    boolean doesFileExist(final String filename, final FileType type) throws IOException;

    boolean deleteFile(final String filename, final FileType type) throws IOException;

    byte[] getFile(final String filename, final FileType type) throws IOException;
}
