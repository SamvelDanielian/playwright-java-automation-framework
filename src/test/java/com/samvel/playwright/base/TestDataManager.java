package com.samvel.playwright.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Properties;

public final class TestDataManager {

    private static final Properties CREDENTIALS = new Properties();

    static {
        try (InputStream in = TestDataManager.class.getClassLoader().getResourceAsStream("testdata/credentials.properties")) {
            if (in != null) {
                CREDENTIALS.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load testdata/credentials.properties", e);
        }
    }

    private TestDataManager() {
    }

    private static String resolve(String key) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }
        String envVar = System.getenv(key.toUpperCase().replace('.', '_'));
        if (envVar != null && !envVar.isBlank()) {
            return envVar;
        }
        return CREDENTIALS.getProperty(key);
    }

    public static String validUsername() {
        return resolve("valid.username");
    }

    public static String validPassword() {
        return resolve("valid.password");
    }

    public static String invalidUsername() {
        return resolve("invalid.username");
    }

    public static String invalidPassword() {
        return resolve("invalid.password");
    }

    public static String nonExistentEmployeeName() {
        return "Zzz_NoSuchEmployee_" + Instant.now().getEpochSecond();
    }

    public static String uniqueFirstName() {
        return "QA" + Instant.now().getEpochSecond();
    }

    public static final String UNIQUE_LAST_NAME = "AutomationTest";
}
