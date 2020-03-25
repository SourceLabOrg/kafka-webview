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

package org.sourcelab.kafka.webview.ui.repository;

import org.sourcelab.kafka.webview.ui.model.SerializerFormat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * For access records on the message_format table.
 */
@Repository
public interface SerializerFormatRepository extends CrudRepository<SerializerFormat, Long> {
    /**
     * Retrieve by name.
     * @param name Name to search for.
     * @return SerializerFormat found, or null.
     */
    SerializerFormat findByName(final String name);

    /**
     * Find all message formats ordered by name.
     * @return all serializer Formats ordered by name.
     */
    Iterable<SerializerFormat> findAllByOrderByNameAsc();

    /**
     * Find all message formats by type, ordered by name.
     * @param isDefault Only return items that match the is_default field being true or false.
     * @return all serializer formats ordered by name.
     */
    Iterable<SerializerFormat> findByIsDefaultOrderByNameAsc(final boolean isDefault);
}
