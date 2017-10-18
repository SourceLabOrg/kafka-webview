package com.darksci.kafkaview.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

    @Value("${app.name}")
    private String name;

    @Value("${app.uploadPath}")
    private String uploadPath;

    @Value("${app.key}")
    private String appKey;

    public String getName() {
        return name;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public String getAppKey() {
        return appKey;
    }
}
