package com.darksci.kafkaview.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AppProperties {

    @Value("${app.name}")
    private String name;

    @Value("${app.uploadPath}")
    private String uploadPath;

    private Map<String, String> globalProperties;

    public String getName() {
        return name;
    }

    public String getUploadPath() {
        return uploadPath;
    }
}
