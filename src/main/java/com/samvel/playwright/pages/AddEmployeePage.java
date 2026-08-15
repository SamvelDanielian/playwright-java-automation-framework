package com.samvel.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class AddEmployeePage {

    private final Page page;
    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator saveButton;
    private final Locator requiredFieldErrors;

    public AddEmployeePage(Page page) {
        this.page = page;
        this.firstNameInput = page.getByPlaceholder("First Name");
        this.lastNameInput = page.getByPlaceholder("Last Name");
        this.saveButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
        this.requiredFieldErrors = page.getByText("Required");
    }

    public AddEmployeePage fillFirstName(String firstName) {
        firstNameInput.fill(firstName);
        return this;
    }

    public AddEmployeePage fillLastName(String lastName) {
        lastNameInput.fill(lastName);
        return this;
    }

    public EmployeeDetailsPage save() {
        saveButton.click();
        page.waitForURL("**/viewPersonalDetails/**");
        return new EmployeeDetailsPage(page);
    }

    public AddEmployeePage submitWithoutSaving() {
        saveButton.click();
        return this;
    }

    public Locator requiredFieldErrors() {
        return requiredFieldErrors;
    }
}