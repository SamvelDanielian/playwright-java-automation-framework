package com.samvel.playwright.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public final class ConfigManager {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPERTIES.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load config.properties", e);
        }
    }

    private ConfigManager() {
    }

    public static String get(String key, String defaultValue) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String envVar = System.getenv(key.toUpperCase().replace('.', '_'));
        if (envVar != null && !envVar.isBlank()) {
            return envVar;
        }

        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static String getBrowser() {
        return get("browser", "chromium");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(get("headless", "true"));
    }

    public static String getBaseUrl() {
        return get("baseUrl", "https://opensource-demo.orangehrmlive.com/");
    }
}