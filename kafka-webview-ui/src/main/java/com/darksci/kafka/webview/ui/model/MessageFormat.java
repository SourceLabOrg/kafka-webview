package com.darksci.kafka.webview.ui.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MessageFormat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String classpath;

    @Column(nullable = false, unique = true)
    private String jar;

    @Column(nullable = false)
    private boolean isDefaultFormat = false;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(final String classpath) {
        this.classpath = classpath;
    }

    public String getJar() {
        return jar;
    }

    public void setJar(final String jar) {
        this.jar = jar;
    }

    public boolean isDefaultFormat() {
        return isDefaultFormat;
    }

    public void setDefaultFormat(final boolean defaultFormat) {
        isDefaultFormat = defaultFormat;
    }

    @Override
    public String toString() {
        return "MessageFormat{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", classpath='" + classpath + '\'' +
            ", jar='" + jar + '\'' +
            ", isDefaultFormat=" + isDefaultFormat +
            '}';
    }
}
