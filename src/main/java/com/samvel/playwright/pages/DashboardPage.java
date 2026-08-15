package com.samvel.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.samvel.playwright.components.SidebarComponent;

public class DashboardPage {

    private final Page page;
    public final SidebarComponent sidebar;

    private final Locator dashboardHeading;
    private final Locator userDropdownTrigger;
    private final Locator logoutLink;

    public DashboardPage(Page page) {
        this.page = page;
        this.sidebar = new SidebarComponent(page);
        this.dashboardHeading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Dashboard"));
        this.userDropdownTrigger = page.locator(".oxd-userdropdown-tab");
        this.logoutLink = page.getByText("Logout");
    }

    public Locator heading() {
        return dashboardHeading;
    }

    public String currentUrl() {
        return page.url();
    }

    public LoginPage logout() {
        userDropdownTrigger.click();
        logoutLink.click();
        return new LoginPage(page);
    }
}