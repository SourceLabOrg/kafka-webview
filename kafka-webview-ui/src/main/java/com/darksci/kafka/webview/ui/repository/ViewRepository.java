/**
 * MIT License
 *
 * Copyright (c) 2017 Stephen Powis https://github.com/Crim/kafka-webview
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

package com.darksci.kafka.webview.ui.repository;

import com.darksci.kafka.webview.ui.model.View;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * For interacting w/ the View database table.
 */
@Repository
public interface ViewRepository extends CrudRepository<View, Long> {
    /**
     * Retrieve a view by its name.
     */
    View findByName(final String name);

    /**
     * Retrieve all views ordered by name.
     */
    Iterable<View> findAllByOrderByNameAsc();

    /**
     * Retrieve all views ordered by name for a given clusterId.
     */
    Iterable<View> findAllByClusterIdOrderByNameAsc(final long clusterId);

    /**
     * Count how many views exist for a given clusterId.
     */
    Long countByClusterId(final long clusterId);

    /**
     * Find any views that use the specified message format.
     */
    Iterable<View> findAllByKeyMessageFormatIdOrValueMessageFormatIdOrderByNameAsc(final long messageFormatId, final long messageFormatId2);
}
