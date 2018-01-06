/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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
        // Ensure parent directories exist
        Files.createDirectories(Paths.get(filename).getParent());

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
