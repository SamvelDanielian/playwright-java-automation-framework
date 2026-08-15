package com.samvel.playwright.listeners;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.samvel.playwright.base.BaseTest;
import com.samvel.playwright.browser.BrowserManager;
import com.samvel.playwright.config.ConfigManager;
import com.samvel.playwright.utils.FailureDiagnostics;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestLifecycleExtension implements BeforeEachCallback, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(TestLifecycleExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL)
                .getOrComputeIfAbsent("browser-manager-shutdown-hook",
                        key -> (ExtensionContext.Store.CloseableResource) BrowserManager::close);

        Browser browser = BrowserManager.getBrowser();
        BrowserContext browserContext = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL(ConfigManager.getBaseUrl())
                .setViewportSize(1440, 900));

        browserContext.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        Page page = browserContext.newPage();

        context.getStore(NAMESPACE).put("context", browserContext);

        Object testInstance = context.getRequiredTestInstance();
        if (testInstance instanceof BaseTest baseTest) {
            baseTest.setPage(page);
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        BrowserContext browserContext = context.getStore(NAMESPACE).get("context", BrowserContext.class);
        boolean failed = context.getExecutionException().isPresent();
        String testName = context.getRequiredTestClass().getSimpleName() + "_" + context.getRequiredTestMethod().getName();

        try {
            if (failed) {
                Object testInstance = context.getRequiredTestInstance();
                if (testInstance instanceof BaseTest baseTest) {
                    FailureDiagnostics.captureScreenshot(baseTest.getPage(), testName);
                }
                browserContext.tracing().stop(new Tracing.StopOptions().setPath(FailureDiagnostics.tracePath(testName)));
            } else {
                browserContext.tracing().stop();
            }
        } finally {
            browserContext.close();
        }
    }
}