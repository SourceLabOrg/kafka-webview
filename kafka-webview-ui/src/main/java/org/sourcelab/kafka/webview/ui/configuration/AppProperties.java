package org.sourcelab.kafka.webview.ui.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Value class for configuration values.
 */
@Component
public class AppProperties {

    @Value("${app.name}")
    private String name;

    @Value("${app.uploadPath}")
    private String uploadPath;

    @Value("${app.key}")
    private String appKey;

    @Value("${app.maxConcurrentWebSocketConsumers}")
    private Integer maxConcurrentWebSocketConsumers = 100;

    public String getName() {
        return name;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public String getAppKey() {
        return appKey;
    }

    public Integer getMaxConcurrentWebSocketConsumers() {
        return maxConcurrentWebSocketConsumers;
    }

    @Override
    public String toString() {
        return "AppProperties{"
            + "name='" + name + '\''
            + ", uploadPath='" + uploadPath + '\''
            + ", appKey='" + appKey + '\''
            + ", maxConcurrentWebSocketConsumers=" + maxConcurrentWebSocketConsumers
            + '}';
    }
}
