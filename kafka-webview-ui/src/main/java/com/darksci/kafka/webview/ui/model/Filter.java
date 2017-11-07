package com.darksci.kafka.webview.ui.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a row in the filter table.
 */
@Entity
public class Filter {
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
    private String options = "";

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

    public String getOptions() {
        return options;
    }

    public void setOptions(final String options) {
        this.options = options;
    }

    /**
     * @return All of the option names, as a set.
     */
    @Transient
    public Set<String> getOptionsAsSet() {
        final Set<String> set = new HashSet<>();
        Collections.addAll(set, getOptions().split(","));
        return set;
    }

    @Override
    public String toString() {
        return "Filter{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", classpath='" + classpath + '\''
            + ", jar='" + jar + '\''
            + ", options='" + options + '\''
            + '}';
    }
}
