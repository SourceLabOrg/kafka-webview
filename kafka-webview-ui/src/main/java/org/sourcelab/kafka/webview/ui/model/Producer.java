package org.sourcelab.kafka.webview.ui.model;

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

import org.hibernate.annotations.Cascade;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import java.sql.Timestamp;

/**
 * Producer Class.
 */
@Entity
public class Producer {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cluster cluster;

    //TODO uncomment when we want to send more than a map of string/string as a kafka message
//    @ManyToOne(fetch = FetchType.LAZY)
//    private MessageFormat keyMessageFormat;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    private MessageFormat valueMessageFormat;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp updatedAt;

    @JoinColumn(name = "id", referencedColumnName = "producer_id")
    @OneToOne(fetch = FetchType.LAZY)
    @Cascade( org.hibernate.annotations.CascadeType.DELETE )
    private ProducerMessage producerMessage;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster( Cluster cluster ) {
        this.cluster = cluster;
    }
// TODO uncomment when we want to send more than a map of string/string as a kafka message

//    public MessageFormat getKeyMessageFormat()
//    {
//        return keyMessageFormat;
//    }
//
//    public void setKeyMessageFormat( MessageFormat keyMessageFormat )
//    {
//        this.keyMessageFormat = keyMessageFormat;
//    }
//
//    public MessageFormat getValueMessageFormat()
//    {
//        return valueMessageFormat;
//    }
//
//    public void setValueMessageFormat( MessageFormat valueMessageFormat )
//    {
//        this.valueMessageFormat = valueMessageFormat;
//    }

    public String getTopic() {
        return topic;
    }

    public void setTopic( String topic ) {
        this.topic = topic;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt( Timestamp createdAt ) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt( Timestamp updatedAt ) {
        this.updatedAt = updatedAt;
    }

    public ProducerMessage getProducerMessage() {
        return producerMessage;
    }

    public void setProducerMessage( ProducerMessage producerMessage ) {
        this.producerMessage = producerMessage;
    }
}
