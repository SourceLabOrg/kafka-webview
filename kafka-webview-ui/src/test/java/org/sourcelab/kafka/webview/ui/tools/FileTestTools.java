package org.sourcelab.kafka.webview.ui.tools;

import com.google.common.base.Charsets;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileTestTools {
    /**
     * Helper for writing a dummy file.
     * @param filename Filename to write
     * @param contents Contents of the file
     */
    public static void createDummyFile(final String filename, final String contents) throws IOException {
        try (final FileOutputStream outputStream = new FileOutputStream(filename)) {
            outputStream.write(contents.getBytes(Charsets.UTF_8));
        }
    }

    /**
     * Utility method to read the contents of a file.
     * @param filename Filename to read
     * @return Contents of the file.
     * @throws IOException
     */
    public static String readFile(final String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)), Charsets.UTF_8);
    }
}
