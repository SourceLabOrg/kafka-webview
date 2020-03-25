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

package org.sourcelab.kafka.webview.ui.manager.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for reporting dependencies.
 */
public interface EntityUsageManager {

    /**
     * Given an instance Id, return a collection of any places that instance is used as a child dependency.
     * @param id the instance id.
     * @return Collection of usages.
     */
    Collection<Usage> findUsages(final long id);

    /**
     * Defines a parent entity type and the instances of it that use the child relationship.
     */
    class Usage {
        private final String entityType;
        private final Collection<UsageInstance> instances;

        /**
         * Private Constructor -- Use Builder interface.
         * @param entityType type of entity.
         * @param usages all of the places its used.
         */
        private Usage(final String entityType, final Collection<UsageInstance> usages) {
            this.entityType = entityType;
            this.instances = usages;
        }

        public String getEntityType() {
            return entityType;
        }

        public Collection<UsageInstance> getInstances() {
            return instances;
        }

        @Override
        public String toString() {
            return getEntityType() + ": " + getInstances().toString();
        }

        /**
         * Create new builder instance.
         * @return new builder instance.
         */
        public static UsageBuilder newBuilder() {
            return new UsageBuilder();
        }
    }

    /**
     * Defines a usage.
     */
    class UsageInstance {
        private final String name;
        private final long id;

        /**
         * Private constructor.
         * @param name Name of the instance.
         * @param id of the instance.
         */
        private UsageInstance(final String name, final long id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public long getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * For constructing usages.
     */
    class UsageBuilder {
        private final Map<String, Collection<UsageInstance>> instances = new HashMap<>();

        /**
         * Add a new instance to the builder.
         * @param entityType Name of the entity, example: "View", or "Cluster"
         * @param entityName name of the instance. example: view.name field.
         * @param entityId Id of the instance. Example view.id field.
         * @return builder for chaining.
         */
        public UsageBuilder withInstance(final String entityType, final String entityName, final long entityId) {
            if (!instances.containsKey(entityType)) {
                instances.put(entityType, new ArrayList<>());
            }
            instances.get(entityType).add(new UsageInstance(entityName, entityId));
            return this;
        }

        /**
         * Build instance.
         * @return collection of usages.
         */
        public Collection<Usage> build() {
            if (instances.isEmpty()) {
                return Collections.emptyList();
            }

            final List<Usage> usages = new ArrayList<>();
            for (final Map.Entry<String, Collection<UsageInstance>> entry : instances.entrySet()) {
                usages.add(new Usage(entry.getKey(), entry.getValue()));
            }
            return Collections.unmodifiableCollection(usages);
        }
    }
}
