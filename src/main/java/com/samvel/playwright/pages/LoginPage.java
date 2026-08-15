package com.samvel.playwright.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage {

    private final Page page;
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator invalidCredentialsAlert;
    private final Locator requiredFieldErrors;

    public LoginPage(Page page) {
        this.page = page;
        this.usernameInput = page.getByPlaceholder("Username");
        this.passwordInput = page.getByPlaceholder("Password");
        this.loginButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
        this.invalidCredentialsAlert = page.getByText("Invalid credentials");
        this.requiredFieldErrors = page.getByText("Required");
    }

    public LoginPage navigate() {
        page.navigate("/web/index.php/auth/login");
        return this;
    }

    public DashboardPage loginAs(String username, String password) {
        usernameInput.fill(username);
        passwordInput.fill(password);
        loginButton.click();
        return new DashboardPage(page);
    }

    public LoginPage attemptLogin(String username, String password) {
        usernameInput.fill(username);
        passwordInput.fill(password);
        loginButton.click();
        return this;
    }

    public LoginPage submit() {
        loginButton.click();
        return this;
    }

    public Locator invalidCredentialsAlert() {
        return invalidCredentialsAlert;
    }

    public Locator requiredFieldErrors() {
        return requiredFieldErrors;
    }
}