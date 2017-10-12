package com.darksci.kafkaview.configuration;

import com.darksci.kafkaview.manager.plugin.DeserializerLoader;
import com.darksci.kafkaview.manager.plugin.PluginFactory;
import com.darksci.kafkaview.manager.plugin.PluginSecurityPolicy;
import com.darksci.kafkaview.manager.plugin.PluginUploadManager;
import com.darksci.kafkaview.plugin.filter.RecordFilter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


import java.security.Policy;

@Component
public class PluginConfig implements ApplicationListener<ApplicationReadyEvent> {

    private final Policy pluginSecurityPolicy = new PluginSecurityPolicy();

    @Bean
    public Policy getPluginSecurityPolicy() {
        return pluginSecurityPolicy;
    }

    @Bean
    public PluginUploadManager getPluginUploadManager(final AppProperties appProperties) {
        return new PluginUploadManager(appProperties.getJarUploadPath());
    }

    @Bean
    public DeserializerLoader getDeserializerLoader(final AppProperties appProperties) {
        return new DeserializerLoader(appProperties.getJarUploadPath() + "/deserializers");
    }

    @Bean
    public PluginFactory<RecordFilter> getRecordFilterPluginFactory(final AppProperties appProperties) {
        final String jarDirectory = appProperties.getJarUploadPath() + "/filters";
        return new PluginFactory<>(jarDirectory, RecordFilter.class);
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
//        // Setup plugin policy on startup
//        Policy.setPolicy(getPluginSecurityPolicy());
//        System.setSecurityManager(new SecurityManager());
    }
}
