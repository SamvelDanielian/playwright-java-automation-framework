package com.samvel.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class EmployeeDetailsPage {

    private final Page page;
    private final Locator personalDetailsHeading;

    public EmployeeDetailsPage(Page page) {
        this.page = page;
        this.personalDetailsHeading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Personal Details"));
    }

    public Locator personalDetailsHeading() {
        return personalDetailsHeading;
    }

    public Locator employeeIdInput() {
        return page
                .locator("div.oxd-input-group")
                .filter(new Locator.FilterOptions().setHasText("Employee Id"))
                .locator("input");
    }

    public Locator fullNameText(String fullName) {
        return page.getByText(fullName).first();
    }

    public String currentUrl() {
        return page.url();
    }
}