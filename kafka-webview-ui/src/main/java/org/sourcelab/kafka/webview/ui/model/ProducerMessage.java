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
package org.sourcelab.kafka.webview.ui.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class ProducerMessage
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String qualifiedClassName;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp updatedAt;

    @JoinColumn(name = "producer_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.LAZY)
    private Producer producer;

    @Column(nullable = false)
    private String propertyNameList;

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

    public String getQualifiedClassName()
    {
        return qualifiedClassName;
    }

    public void setQualifiedClassName( String qualifiedClassName )
    {
        this.qualifiedClassName = qualifiedClassName;
    }

    public Timestamp getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt( Timestamp createdAt )
    {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt( Timestamp updatedAt )
    {
        this.updatedAt = updatedAt;
    }

    public Producer getProducer()
    {
        return producer;
    }

    public void setProducer( Producer producer )
    {
        this.producer = producer;
    }

    public String getPropertyNameList()
    {
        return propertyNameList;
    }

    public void setPropertyNameList( String propertyNameList )
    {
        this.propertyNameList = propertyNameList;
    }

    public String[] getPropertyNameListAsArray()
    {
        return propertyNameList.split( "," );
    }
}
