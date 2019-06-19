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

package org.sourcelab.kafka.webview.ui.plugin.deserializer;

import java.util.Collection;

/**
 * Discovery service for deserializer auto configuration.
 *
 * Adding / implementing this interface on a {@link org.apache.kafka.common.serialization.Deserializer} implementation
 * will allow Kafka-WebView to detect it on startup and automatically register it within the application.
 *
 * On Startup Kafka-WebView will scan for JARs that contain classes that implement this interface.
 * If it finds any, it will use the information returned from getDeserializersInformation() to attempt to register it.
 *
 * If an existing Deserializer exists with the same name:
 *   - If the Deserializer instance was created via this Discovery mechanism, it will be updated with the most current values.
 *   - If the Deserializer instance was NOT created via this Discovery mechanism, it will be skipped/ignored.
 */
public interface DeserializerDiscoveryService {
    /**
     * Return registration information for a Deserializer.
     * @return Collection of registration information.
     */
    Collection<DeserializerInformation> getDeserializersInformation();
}
