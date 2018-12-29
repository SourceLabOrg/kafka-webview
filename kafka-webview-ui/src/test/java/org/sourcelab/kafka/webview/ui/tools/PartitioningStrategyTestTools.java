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

import org.sourcelab.kafka.webview.ui.model.PartitioningStrategy;
import org.sourcelab.kafka.webview.ui.repository.PartitioningStrategyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helpful tools for Filters in tests.
 */
@Component
public class PartitioningStrategyTestTools {
    private final PartitioningStrategyRepository partitioningStrategyRepository;

    @Autowired
    public PartitioningStrategyTestTools(final PartitioningStrategyRepository partitioningStrategyRepository) {
        this.partitioningStrategyRepository = partitioningStrategyRepository;
    }

    /**
     * Utility for creating partitioning strategies.
     * @param name Name of the strategy.
     * @return Persisted Strategy.
     */
    public PartitioningStrategy createStrategy(final String name) {
        final PartitioningStrategy strategy = new PartitioningStrategy();
        strategy.setName(name);
        strategy.setClasspath("com.example." + name);
        strategy.setJar(name + ".jar");
        strategy.setOptionParameters("{\"key\": \"value\"}");
        partitioningStrategyRepository.save(strategy);

        return strategy;
    }
}
