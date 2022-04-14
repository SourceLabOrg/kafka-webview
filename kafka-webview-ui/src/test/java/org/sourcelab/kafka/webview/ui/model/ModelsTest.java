/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.repository.FilterRepository;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.sql.Timestamp;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ModelsTest {

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private MessageFormatRepository messageFormatRepository;

    @Autowired
    private FilterRepository filterRepository;

    @Autowired
    private ViewRepository viewRepository;

    /**
     * Smoke test over models.
     */
    @Test
    @Transactional
    public void smokeTest() {
        final String token = String.valueOf(System.currentTimeMillis());
        final Timestamp now = new Timestamp(System.currentTimeMillis());

        // Create a cluster
        final Cluster cluster = new Cluster();
        cluster.setName("My Cluster " + token);
        cluster.setBrokerHosts("localhost:90902");
        clusterRepository.save(cluster);

        // Create a message format
        final MessageFormat messageFormat = new MessageFormat();
        messageFormat.setName("My MessageFormat " + token);
        messageFormat.setClasspath("com.example.random.class");
        messageFormat.setJar("MyJar.jar");
        messageFormat.setDefaultFormat(false);
        messageFormatRepository.save(messageFormat);

        // Create an "Optional" filter
        final Filter filter = new Filter();
        filter.setName("Optional Filter " + token);
        filter.setClasspath("com.example.filter.class");
        filter.setJar("MyFilterJar.jar");
        filter.setOptions("option1,option2,option3");
        filterRepository.save(filter);

        // Create an "Enforced" filter
        final Filter filter2 = new Filter();
        filter2.setName("Enforced Filter " + token);
        filter2.setClasspath("com.example.filter.class");
        filter2.setJar("MyFilterJar.jar");
        filter2.setOptions("option4");
        filterRepository.save(filter2);

        // Create a view
        final View view = new View();
        view.setName("My View " + token);
        view.setCluster(cluster);
        view.setTopic("my_topic_name");
        view.setKeyMessageFormat(messageFormat);
        view.setValueMessageFormat(messageFormat);
        view.setResultsPerPartition(10);
        view.setPartitions("");
        view.setCreatedAt(now);
        view.setUpdatedAt(now);

        // Add Optional Filter
        final ViewToFilterOptional optionalFilter = new ViewToFilterOptional();
        optionalFilter.setFilter(filter);
        optionalFilter.setView(view);
        optionalFilter.setSortOrder(0L);
        view.getOptionalFilters().add(optionalFilter);

        // Add EnforcedFilter
        final ViewToFilterEnforced enforcedFilter = new ViewToFilterEnforced();
        enforcedFilter.setFilter(filter2);
        enforcedFilter.setView(view);
        enforcedFilter.setOptionParameters("{\"option4\"=\"myvalue\"}");
        enforcedFilter.setSortOrder(0L);
        view.getEnforcedFilters().add(enforcedFilter);

        // Save view and related.
        viewRepository.save(view);

        // Clean up
        viewRepository.delete(view);
        filterRepository.delete(filter);
        filterRepository.delete(filter2);
        messageFormatRepository.delete(messageFormat);
        clusterRepository.delete(cluster);
    }
}