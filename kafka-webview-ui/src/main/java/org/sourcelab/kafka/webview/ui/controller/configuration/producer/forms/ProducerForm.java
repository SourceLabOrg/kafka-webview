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
package org.sourcelab.kafka.webview.ui.controller.configuration.producer.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ProducerForm
{
    private Long id = null;

    @NotNull( message = "Enter a unique name")
    @Size( min = 2, max = 255)
    private String name;

    @NotNull(message = "Select a cluster")
    private Long clusterId;
//    TODO uncomment when we want to send more than a map of string/string as a kafka message
//    @NotNull(message = "Select a message format")
//    private Long keyMessageFormatId;
//
//    @NotNull(message = "Select a message format")
//    private Long valueMessageFormatId;

    @NotNull(message = "Select a topic")
    @Size(min = 1, max = 255)
    private String topic;

    @NotNull(message = "A producer must have property names to send a message")
    private String producerMessagePropertyNameList;

    @NotNull(message = "A producer message must reference an existing class in the platform")
    private String producerMessageClassName;

    public Long getId()
    {
        return id;
    }

    public void setId( Long id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public Long getClusterId()
    {
        return clusterId;
    }

    public void setClusterId( Long clusterId )
    {
        this.clusterId = clusterId;
    }
//    TODO uncomment when we want to send more than a map of string/string as a kafka message

//    public Long getKeyMessageFormatId()
//    {
//        return keyMessageFormatId;
//    }
//
//    public void setKeyMessageFormatId( Long keyMessageFormatId )
//    {
//        this.keyMessageFormatId = keyMessageFormatId;
//    }
//
//    public Long getValueMessageFormatId()
//    {
//        return valueMessageFormatId;
//    }
//
//    public void setValueMessageFormatId( Long valueMessageFormatId )
//    {
//        this.valueMessageFormatId = valueMessageFormatId;
//    }

    public String getTopic()
    {
        return topic;
    }

    public void setTopic( String topic )
    {
        this.topic = topic;
    }

    public String getProducerMessagePropertyNameList()
    {
        return producerMessagePropertyNameList;
    }

    public void setProducerMessagePropertyNameList( String producerMessagePropertyNameList )
    {
        this.producerMessagePropertyNameList = producerMessagePropertyNameList;
    }

    public String getProducerMessageClassName()
    {
        return producerMessageClassName;
    }

    public void setProducerMessageClassName( String producerMessageClassName )
    {
        this.producerMessageClassName = producerMessageClassName;
    }

    public boolean exists() {
        return getId() != null;
    }

    public String[] getPropertyNameListAsArray()
    {
        return producerMessagePropertyNameList.split( "," );
    }

    @Override
    public String toString()
    {
        return "ProductForm{id=" + id +
               ",name='" + name + '\'' +
               ",clusterId=" + clusterId +
//               TODO uncomment when we want to send more than a map of string/string as a kafka message
//               ",keyMessageFormatId=" + keyMessageFormatId +
//               ",valueMessageFormatId=" + valueMessageFormatId +
               ",topic='" + topic + '\'' +
               ",producerMessageKeys='" + producerMessagePropertyNameList + '\'' +
               ",producerMessageClassName='" + producerMessageClassName + '\'' +
               '}';
    }
}
