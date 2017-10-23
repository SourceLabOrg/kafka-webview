package com.darksci.kafka.webview.manager.kafka.dto;

public class ConfigItem {
    private final String name;
    private final String value;
    private final boolean isDefault;

    public ConfigItem(final String name, final String value, final boolean isDefault) {
        this.name = name;
        this.value = value;
        this.isDefault = isDefault;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String toString() {
        return "ConfigItem{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            ", isDefault=" + isDefault +
            '}';
    }
}
