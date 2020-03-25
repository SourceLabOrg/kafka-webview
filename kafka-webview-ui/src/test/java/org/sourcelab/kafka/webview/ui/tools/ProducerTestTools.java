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
package org.sourcelab.kafka.webview.ui.tools;

import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.Producer;
import org.sourcelab.kafka.webview.ui.model.ProducerMessage;
import org.sourcelab.kafka.webview.ui.repository.ProducerMessageRepository;
import org.sourcelab.kafka.webview.ui.repository.ProducerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.UUID;

@Component
public class ProducerTestTools
{
    private final ClusterTestTools clusterTestTools;
    private final ProducerRepository producerRepository;
    private final ProducerMessageRepository producerMessageRepository;

    @Autowired
    public ProducerTestTools(
        final ClusterTestTools clusterTestTools,
        final ProducerRepository producerRepository,
        final ProducerMessageRepository producerMessageRepository
    )
    {
        this.clusterTestTools = clusterTestTools;
        this.producerRepository = producerRepository;
        this.producerMessageRepository = producerMessageRepository;
    }

    public Producer createProducer(boolean persist)
    {
        return createProducer( UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", persist );
    }

    public Producer createProducer(final String name, final String topic, final String messagePropertyNameList, final Cluster cluster)
    {
        ProducerMessage producerMessage = new ProducerMessage();
        Producer producer = new Producer();

        producer.setName( name );
        producer.setCluster( cluster );
        producer.setTopic( topic );
        producer.setCreatedAt( new Timestamp( System.currentTimeMillis() ) );
        producer.setUpdatedAt( new Timestamp( System.currentTimeMillis() ) );

        producerRepository.save( producer );

        producerMessage.setProducer( producer );
        producerMessage.setName( producer.getName() + "-message" );
        producerMessage.setQualifiedClassName( "com.test.QualifiedClassName" );
        producerMessage.setPropertyNameList( messagePropertyNameList );
        producerMessage.setCreatedAt( new Timestamp( System.currentTimeMillis() ) );
        producerMessage.setUpdatedAt( new Timestamp( System.currentTimeMillis() ) );
        producerMessageRepository.save( producerMessage );

        return producer;
    }

    public Producer createProducer(final String name, final String topic, final String messagePropertyNameList, boolean persist)
    {
        String clusterName = UUID.randomUUID().toString();
        ProducerMessage producerMessage = new ProducerMessage();
        Cluster cluster = clusterTestTools.createCluster( clusterName );
        Producer producer = new Producer();

        producer.setName( name );
        producer.setCluster( cluster );
        producer.setTopic( topic );
        producer.setCreatedAt( new Timestamp( System.currentTimeMillis() ) );
        producer.setUpdatedAt( new Timestamp( System.currentTimeMillis() ) );
        if(persist)
        {
            producerRepository.save( producer );

            producerMessage.setProducer( producer );
            producerMessage.setName( producer.getName() + "-message" );
            producerMessage.setQualifiedClassName( "com.test.QualifiedClassName" );
            producerMessage.setPropertyNameList( messagePropertyNameList );
            producerMessage.setCreatedAt( new Timestamp( System.currentTimeMillis() ) );
            producerMessage.setUpdatedAt( new Timestamp( System.currentTimeMillis() ) );
            producerMessageRepository.save( producerMessage );
        }

        return producer;
    }

    public void deleteAllProducers()
    {
        producerRepository.deleteAll();
    }

}
