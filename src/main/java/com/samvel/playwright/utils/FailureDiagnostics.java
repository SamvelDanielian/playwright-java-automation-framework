package com.samvel.playwright.utils;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FailureDiagnostics {

    private static final Path SCREENSHOT_DIR = Paths.get("target", "screenshots");
    private static final Path TRACE_DIR = Paths.get("target", "traces");

    static {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            Files.createDirectories(TRACE_DIR);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create failure artifact directories", e);
        }
    }

    private FailureDiagnostics() {
    }

    public static void captureScreenshot(Page page, String testName) {
        byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                .setPath(SCREENSHOT_DIR.resolve(testName + ".png"))
                .setFullPage(true));
        Allure.addAttachment(testName + "-screenshot", new ByteArrayInputStream(screenshot));
    }

    public static Path tracePath(String testName) {
        return TRACE_DIR.resolve(testName + ".zip");
    }
}
