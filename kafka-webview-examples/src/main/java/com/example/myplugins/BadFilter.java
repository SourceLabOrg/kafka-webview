package com.example.myplugins;

import com.darksci.kafkaview.plugin.filter.RecordFilter;
import com.google.gson.Gson;

import java.util.Map;

public class BadFilter implements RecordFilter {

    @Override
    public boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value) {
        final String content = (String) value;

//        Gson gson = new Gson();
//        gson.fromJson(content, Pojo.class);
        try {
            getClass().getClassLoader().loadClass("com.darksci.kafkaview.manager.kafka.config.ClusterConfig");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void configure(final Map<String, ?> configs) {

    }
}
