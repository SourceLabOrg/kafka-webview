package com.darksci.kafka.webview.ui.controller.configuration.filter.forms;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents create/update a Filter form.
 */
public class FilterForm {
    private Long id = null;

    @NotNull(message = "Enter a unique name")
    @Size(min = 2, max = 255)
    private String name;

    @NotNull(message = "Enter a classpath")
    @Size(min = 2, max = 1024)
    private String classpath;

    private MultipartFile file;

    private String optionsJsonStr = "{}";

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
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

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(final MultipartFile file) {
        this.file = file;
    }

    public String getOptionsJsonStr() {
        return optionsJsonStr;
    }

    public void setOptionsJsonStr(final String optionsJsonStr) {
        this.optionsJsonStr = optionsJsonStr;
    }

    /**
     * Does the filter that this form represents already exist.
     */
    public boolean exists() {
        return getId() != null;
    }

    @Override
    public String toString() {
        return "FilterForm{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", classpath='" + classpath + '\''
            + ", file=" + file
            + '}';
    }
}
