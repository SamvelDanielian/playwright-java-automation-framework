package com.samvel.playwright.tests;

import com.samvel.playwright.base.BaseTest;
import com.samvel.playwright.base.TestDataManager;
import com.samvel.playwright.pages.DashboardPage;
import com.samvel.playwright.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@DisplayName("Navigation & Session")
class NavigationSessionTests extends BaseTest {

    private DashboardPage dashboard;

    @BeforeEach
    void loginAsAdmin() {
        dashboard = loginPage()
                .loginAs(TestDataManager.validUsername(), TestDataManager.validPassword());
    }

    @Test
    @DisplayName("The Dashboard is displayed immediately after login")
    void dashboardIsDisplayedAfterLogin() {
        assertThat(dashboard.heading()).isVisible();
    }

    @Test
    @DisplayName("Each sidebar module link navigates to its own module")
    void sidebarNavigatesThroughModules() {
        dashboard
                .sidebar
                .goToModule("PIM");
        assertThat(getPage()).hasURL(Pattern.compile(".*pim.*"));

        dashboard
                .sidebar
                .goToModule("Leave");
        assertThat(getPage()).hasURL(Pattern.compile(".*leave.*"));

        dashboard
                .sidebar
                .goToModule("Admin");
        assertThat(getPage()).hasURL(Pattern.compile(".*admin.*"));
    }

    @Test
    @DisplayName("Logout returns the user to the login page")
    void logoutSucceeds() {
        LoginPage loginPage = dashboard
                .logout();

        assertThat(getPage()).hasURL(Pattern.compile(".*auth/login.*"));
        assertThat(loginPage.requiredFieldErrors()).hasCount(0);
    }

    @Test
    @DisplayName("After logout, a protected page redirects back to the login page")
    void protectedPageRedirectsAfterLogout() {
        dashboard
                .logout();

        getPage()
                .navigate("/web/index.php/pim/viewEmployeeList");

        assertThat(getPage()).hasURL(Pattern.compile(".*auth/login.*"));
    }
}