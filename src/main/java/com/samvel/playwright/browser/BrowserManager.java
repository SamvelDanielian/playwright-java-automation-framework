package com.samvel.playwright.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.samvel.playwright.config.ConfigManager;

public final class BrowserManager {

    private static Playwright playwright;
    private static Browser browser;

    private BrowserManager() {
    }

    public static synchronized Browser getBrowser() {
        if (browser == null) {
            playwright = Playwright.create();
            browser = launch(playwright, ConfigManager.getBrowser(), ConfigManager.isHeadless());
        }
        return browser;
    }

    private static Browser launch(Playwright playwright, String browserName, boolean headless) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(headless);
        return switch (browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            default -> playwright.chromium().launch(options);
        };
    }

    public static synchronized void close() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }
}