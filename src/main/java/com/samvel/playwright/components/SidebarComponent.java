package com.samvel.playwright.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.samvel.playwright.pages.EmployeeListPage;

public class SidebarComponent {

    private final Page page;

    public SidebarComponent(Page page) {
        this.page = page;
    }

    private Locator menuItem(String name) {
        return page.locator(".oxd-main-menu-item").filter(new Locator.FilterOptions().setHasText(name));
    }

    public Locator moduleLink(String name) {
        return menuItem(name);
    }

    public EmployeeListPage goToPim() {
        menuItem("PIM").click();
        return new EmployeeListPage(page);
    }

    public void goToModule(String moduleName) {
        menuItem(moduleName).click();
    }
}