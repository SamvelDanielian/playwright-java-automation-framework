package com.samvel.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class EmployeeListPage {

    private final Page page;
    private final Locator employeeNameInput;
    private final Locator searchButton;
    private final Locator addButton;
    private final Locator noRecordsMessage;
    private final Locator dataRows;

    public EmployeeListPage(Page page) {
        this.page = page;
        this.employeeNameInput = page.locator("div.oxd-input-group")
                .filter(new Locator.FilterOptions().setHasText("Employee Name"))
                .getByPlaceholder("Type for hints...");
        this.searchButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search"));
        this.addButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add"));
        this.noRecordsMessage = page.getByText("No Records Found").first();
        this.dataRows = page.getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.CELL)));
    }

    public EmployeeListPage navigate() {
        page.navigate("/web/index.php/pim/viewEmployeeList");
        return this;
    }

    public Locator addButton() {
        return addButton;
    }

    public Locator noRecordsMessage() {
        return noRecordsMessage;
    }

    public Locator dataRows() {
        return dataRows;
    }

    public Locator rowsMatching(String text) {
        return dataRows.filter(new Locator.FilterOptions().setHasText(text));
    }

    public EmployeeListPage searchByName(String name) {
        employeeNameInput.click();
        employeeNameInput.pressSequentially(name, new Locator.PressSequentiallyOptions().setDelay(50));
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(name)).first().click();
        searchButton.click();
        return this;
    }

    public EmployeeListPage searchByUnmatchedNameText(String freeText) {
        employeeNameInput.click();
        employeeNameInput.fill(freeText);
        searchButton.click();
        return this;
    }

    public AddEmployeePage clickAdd() {
        addButton.click();
        return new AddEmployeePage(page);
    }

    public EmployeeDetailsPage openFirstEmployee() {
        dataRows.first().locator("button").first().click();
        return new EmployeeDetailsPage(page);
    }
}