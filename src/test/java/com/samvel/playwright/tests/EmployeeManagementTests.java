package com.samvel.playwright.tests;

import com.samvel.playwright.base.BaseTest;
import com.samvel.playwright.base.TestDataManager;
import com.samvel.playwright.pages.AddEmployeePage;
import com.samvel.playwright.pages.EmployeeDetailsPage;
import com.samvel.playwright.pages.EmployeeListPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@DisplayName("Employee Management")
class EmployeeManagementTests extends BaseTest {

    private EmployeeListPage employeeListPage;

    @BeforeEach
    void loginAsAdmin() {
        employeeListPage = loginPage()
                .loginAs(TestDataManager.validUsername(), TestDataManager.validPassword())
                .sidebar.goToPim();
    }

    @Test
    @DisplayName("The employee list is displayed with results")
    void employeeListIsDisplayed() {
        assertThat(employeeListPage.addButton()).isVisible();
        assertThat(employeeListPage.dataRows().first()).isVisible();
    }

    @Test
    @DisplayName("Searching for an employee that was just added returns that employee")
    void searchExistingEmployeeReturnsResult() {
        String firstName = TestDataManager
                .uniqueFirstName();
        String fullName = firstName + " " + TestDataManager.UNIQUE_LAST_NAME;

        employeeListPage
                .clickAdd()
                .fillFirstName(firstName)
                .fillLastName(TestDataManager.UNIQUE_LAST_NAME)
                .save();

        EmployeeListPage listAfterAdd = new EmployeeListPage(getPage())
                .navigate();
        listAfterAdd
                .searchByName(fullName);

        assertThat(listAfterAdd.rowsMatching(fullName).first()).isVisible();
    }

    @Test
    @DisplayName("Searching for an employee that does not exist shows 'No Records Found'")
    void searchNonExistentEmployeeShowsNoRecords() {
        employeeListPage
                .searchByUnmatchedNameText(TestDataManager.nonExistentEmployeeName());

        assertThat(employeeListPage.noRecordsMessage()).isVisible();
    }

    @Test
    @DisplayName("Opening an employee from the list navigates to their Personal Details")
    void openEmployeeDetailsFromList() {
        EmployeeDetailsPage detailsPage = employeeListPage
                .openFirstEmployee();

        assertThat(detailsPage.personalDetailsHeading()).isVisible();
        assertThat(getPage()).hasURL(Pattern.compile(".*viewPersonalDetails.*"));
    }

    @Test
    @DisplayName("Adding a new employee succeeds and lands on their Personal Details page")
    void addEmployeeSuccessfully() {
        String firstName = TestDataManager
                .uniqueFirstName();
        String fullName = firstName + " " + TestDataManager.UNIQUE_LAST_NAME;

        EmployeeDetailsPage detailsPage = employeeListPage
                .clickAdd()
                .fillFirstName(firstName)
                .fillLastName(TestDataManager.UNIQUE_LAST_NAME)
                .save();

        assertThat(getPage()).hasURL(Pattern.compile(".*viewPersonalDetails.*"));
        assertThat(detailsPage.personalDetailsHeading()).isVisible();
        assertThat(detailsPage.fullNameText(fullName)).isVisible();
    }

    @Test
    @DisplayName("Saving the Add Employee form with no name shows required-field errors")
    void addEmployeeRequiredFieldValidation() {
        AddEmployeePage addEmployeePage = employeeListPage
                .clickAdd()
                .submitWithoutSaving();

        assertThat(addEmployeePage.requiredFieldErrors().first()).isVisible();
    }
}