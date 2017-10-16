package com.darksci.kafkaview.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class AppProperties {

    @Value("${app.name}")
    private String name;

    @Value("${app.baseUrl}")
    private String baseUrl;

    @Value("${app.systemEmail}")
    private String systemEmail;

    @Value("${app.companyName}")
    private String companyName;

    @Value("${app.companyAddress}")
    private String companyAddress;

    @Value("${app.uploadPath}")
    private String uploadPath;

    private Map<String, String> globalProperties;

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getSystemEmail() {
        return systemEmail;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public Map<String, String> getGlobalProperties() {
        if (globalProperties == null) {
            Map<String, String> tmpMap = new HashMap<>();
            tmpMap.put("app_name", name);
            tmpMap.put("app_baseUrl", baseUrl);
            tmpMap.put("app_systemEmail", systemEmail);
            tmpMap.put("app_companyName", companyName);
            tmpMap.put("app_companyAddress", companyAddress);

            globalProperties = Collections.unmodifiableMap(tmpMap);
        }
        return globalProperties;
    }


    public static final class Builder {
        private String name;
        private String baseUrl;
        private String systemEmail;
        private String companyName;
        private String companyAddress;
        private Map<String, String> globalProperties;

        public Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder withSystemEmail(String systemEmail) {
            this.systemEmail = systemEmail;
            return this;
        }

        public Builder withCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder withCompanyAddress(String companyAddress) {
            this.companyAddress = companyAddress;
            return this;
        }

        public Builder withGlobalProperties(Map<String, String> globalProperties) {
            this.globalProperties = globalProperties;
            return this;
        }

        public AppProperties build() {
            AppProperties appProperties = new AppProperties();
            appProperties.companyName = this.companyName;
            appProperties.systemEmail = this.systemEmail;
            appProperties.companyAddress = this.companyAddress;
            appProperties.baseUrl = this.baseUrl;
            appProperties.name = this.name;
            appProperties.globalProperties = this.globalProperties;
            return appProperties;
        }
    }
}
