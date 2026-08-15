package com.samvel.playwright.tests;

import com.samvel.playwright.base.BaseTest;
import com.samvel.playwright.base.TestDataManager;
import com.samvel.playwright.pages.DashboardPage;
import com.samvel.playwright.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@DisplayName("Authentication")
class LoginTests extends BaseTest {

    @Test
    @DisplayName("A valid Admin login reaches the Dashboard")
    void successfulLoginReachesDashboard() {
        DashboardPage dashboard = loginPage()
                .loginAs(TestDataManager.validUsername(), TestDataManager.validPassword());

        assertThat(dashboard.heading()).isVisible();
        assertThat(getPage()).hasURL(Pattern.compile(".*dashboard/index.*"));
    }

    @Test
    @DisplayName("An invalid username/password combination shows an error and stays on the login page")
    void invalidCredentialsShowError() {
        LoginPage loginPage = loginPage()
                .attemptLogin(TestDataManager.invalidUsername(), TestDataManager.invalidPassword());

        assertThat(loginPage.invalidCredentialsAlert()).isVisible();
        assertThat(getPage()).hasURL(Pattern.compile(".*auth/login.*"));
    }

    @Test
    @DisplayName("Submitting with an empty username shows a required-field error")
    void emptyUsernameShowsRequiredError() {
        LoginPage loginPage = loginPage()
                .attemptLogin("", TestDataManager.validPassword());

        assertThat(loginPage.requiredFieldErrors()).hasCount(1);
    }

    @Test
    @DisplayName("Submitting with an empty password shows a required-field error")
    void emptyPasswordShowsRequiredError() {
        LoginPage loginPage = loginPage()
                .attemptLogin(TestDataManager.validUsername(), "");

        assertThat(loginPage.requiredFieldErrors()).hasCount(1);
    }
}