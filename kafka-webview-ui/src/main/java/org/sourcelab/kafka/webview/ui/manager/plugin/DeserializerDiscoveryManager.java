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

package org.sourcelab.kafka.webview.ui.manager.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.configuration.AppProperties;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.MessageFormatType;
import org.sourcelab.kafka.webview.ui.plugin.deserializer.DeserializerDiscoveryService;
import org.sourcelab.kafka.webview.ui.plugin.deserializer.DeserializerInformation;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Handles discovery and loading of Deserializers that implement the {@link DeserializerDiscoveryService} interface.
 */
@Component
public class DeserializerDiscoveryManager {
    private static final Logger logger = LoggerFactory.getLogger(DeserializerDiscoveryManager.class);

    private final AppProperties appProperties;
    private final ObjectMapper mapper;
    private final MessageFormatRepository messageFormatRepository;

    /**
     * Constructor.
     * @param appProperties  Application Properties.
     * @param mapper For serializing to JSON.
     * @param messageFormatRepository MessageFormatRepository instance.
     */
    @Autowired
    public DeserializerDiscoveryManager(
        final AppProperties appProperties,
        final ObjectMapper mapper, final MessageFormatRepository messageFormatRepository) {
        this.appProperties = Objects.requireNonNull(appProperties);
        this.mapper = mapper;
        this.messageFormatRepository = Objects.requireNonNull(messageFormatRepository);
    }

    /**
     * Discovery any Deserializer instances and generate/update MessageFormat entries for them.
     */
    public void discoverDeserializers() {
        final File deserializerPath = new File(appProperties.getDeserializerPath());

        // Check preconditions
        if (!deserializerPath.exists()) {
            logger.warn("Directory {} doesn't exists.", deserializerPath);
            return;
        }
        if (!deserializerPath.isDirectory()) {
            logger.error("{} is not a directory.", deserializerPath);
            return;
        }

        // find all .jar files.
        final File[] jars = deserializerPath.listFiles((File dir, String name) -> name.endsWith(".jar"));
        logger.info("Analyse {} for deserializer", (Object) jars);

        // iterate over each jar.
        for (final File jar : jars) {
            try {
                final URL jarUrl = jar.toURI().toURL();
                final ClassLoader cl = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());
                final ServiceLoader<DeserializerDiscoveryService> services = ServiceLoader.load(DeserializerDiscoveryService.class, cl);

                // Attempt to load/register them.
                loadDeserializers(jar, services);
            } catch (final MalformedURLException ex) {
                logger.error("Failed to load {}, error: {}", jar, ex.getMessage(), ex);
            }
        }
    }

    private void loadDeserializers(final File jar, final ServiceLoader<DeserializerDiscoveryService> services) {
        for (final DeserializerDiscoveryService service : services) {

            // Pull information about the deserializer.
            final Collection<DeserializerInformation> deserializersInformation = service.getDeserializersInformation();

            // Skip null references
            if (deserializersInformation == null) {
                logger.warn(
                    "Instance returned null DeserializerInformation reference: {} from {}",
                    service.getClass().getSimpleName(),
                    jar.toString()
                );
                continue;
            }

            // Loop over each instance.
            for (final DeserializerInformation deserializerInformation : deserializersInformation) {
                try {
                    // Look for an existing message format with the same name.
                    MessageFormat messageFormat = messageFormatRepository.findByName(deserializerInformation.getName());

                    // If none found
                    if (messageFormat == null) {
                        // We'll create a new one!
                        messageFormat = new MessageFormat();

                    // If we found one but it's not an Autoconfigured instance
                    } else if (MessageFormatType.AUTOCONF != messageFormat.getMessageFormatType()) {
                        // We'll skip it.
                        logger.warn("Try to register the deserializer {} but name is already register as a {}. Skipping.",
                            messageFormat.getName(), messageFormat.getMessageFormatType()
                        );
                        continue;
                    }

                    // Update entry.
                    messageFormat.setName(deserializerInformation.getName());
                    messageFormat.setClasspath(deserializerInformation.getClasspath());
                    messageFormat.setJar(jar.getName());
                    messageFormat.setMessageFormatType(MessageFormatType.AUTOCONF);
                    messageFormat.setOptionParameters(mapper.writeValueAsString(deserializerInformation.getDefaultConfig()));
                    messageFormatRepository.save(messageFormat);
                } catch (final JsonProcessingException ex) {
                    logger.error(
                        "Failed to load {} from {} - The default config properties are invalid ({})",
                        deserializerInformation.getName(), service.getClass().getSimpleName(),
                        ex.getMessage(), ex
                    );
                } catch (final Exception ex) {
                    logger.error(
                        "Failed to load {} from {} - {}",
                        deserializerInformation.getName(), service.getClass().getSimpleName(),
                        ex.getMessage(), ex
                    );
                }
            }
        }
    }
}
