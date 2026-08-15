package com.samvel.playwright.base;

import com.microsoft.playwright.Page;
import com.samvel.playwright.listeners.TestLifecycleExtension;
import com.samvel.playwright.pages.LoginPage;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestLifecycleExtension.class)
public abstract class BaseTest {

    private Page page;

    public void setPage(Page page) {
        this.page = page;
    }

    public Page getPage() {
        return page;
    }

    protected LoginPage loginPage() {
        return new LoginPage(page).navigate();
    }
}