# Playwright OrangeHRM Automation Framework

A portfolio-quality UI automation framework built with **Playwright for Java**, testing the
[OrangeHRM demo application](https://opensource-demo.orangehrmlive.com/). It is a companion to
an existing Selenium Java framework in a separate repository, and is deliberately built around
Playwright's own architecture rather than a Selenium design ported to a new API.

## Application Under Test

**OrangeHRM demo**: https://opensource-demo.orangehrmlive.com/ — a public, shared HR management
system demo. Its own login page publishes demo credentials (`Admin` / `admin123`); these are not
secrets and are safe to keep in version control. Because the instance is public and constantly
written to by other testers worldwide, tests avoid depending on fixed "known" data (e.g. a
specific employee existing forever) — see [Test Data](#test-data--the-shared-demo-environment).

All flows covered by the test suite were manually verified against the live application before
being automated (login validation, employee search behavior, the Add Employee form, sidebar
navigation, logout).

## Why Playwright

This framework exists to demonstrate Playwright fluency, not "Selenium with different syntax":

- **Auto-waiting actionability checks** replace explicit/fluent wait utilities almost entirely.
- **Web-first assertions** (`assertThat(locator)...`) retry until the UI settles instead of
  polling booleans.
- **BrowserContext** gives per-test isolation (cookies, storage, cache) without relaunching the
  Browser process for every test.
- **Tracing** captures a full step-by-step, DOM-snapshotted timeline of a failing test.
- A single Playwright driver process talks to Chromium/Firefox/WebKit through one protocol,
  instead of three different vendor drivers.

## Tech Stack

Java 17 · Playwright for Java · JUnit 5 · Allure · Maven · Docker / Docker Compose · GitHub Actions

## Architecture

```
Tests
  |
Page Objects  (LoginPage, DashboardPage, EmployeeListPage, AddEmployeePage, EmployeeDetailsPage)
  |
Components    (SidebarComponent - shared across every authenticated page)
  |
Playwright Page / Locator
  |
BrowserContext   (fresh per test - isolation)
  |
Browser          (one process, reused for the whole run)
```

```
src/main/java/com/samvel/playwright/
  config/        ConfigManager        - resolves browser/headless/baseUrl (system property > env var > file)
  browser/       BrowserManager       - owns the single Playwright + Browser instance
  pages/         *Page                - Page Objects: locators + actions + page-specific queries
  components/    SidebarComponent     - the main menu, reused by every authenticated page
  utils/         FailureDiagnostics   - single owner of screenshot capture + Allure attachment

src/test/java/com/samvel/playwright/
  base/          BaseTest             - exposes the per-test Page to test classes
  base/          TestDataManager      - loads test data, same override priority as ConfigManager
  listeners/     TestLifecycleExtension - JUnit 5 extension: context/tracing lifecycle + failure capture
  tests/         *Tests               - the 14 test scenarios, grouped by feature

src/test/resources/testdata/
  credentials.properties              - demo login credentials (not secrets)
```

### Why a components layer

There is exactly **one** component, `SidebarComponent`. It exists because the main menu is
identical on every authenticated screen and several tests (navigation, logout, session) need to
drive it independently of whichever page object happens to be "current" — duplicating menu
locators into every page object would mean fixing the same locator in five places. No other UI
piece in this app repeats often enough to earn its own component class, so nothing else was
extracted just to look more "enterprise."

## Page Object Model

Page objects expose **Locators** (via getters) rather than pre-computed booleans, e.g.
`loginPage.invalidCredentialsAlert()` instead of `loginPage.isErrorVisible()`. That lets tests
assert with Playwright's auto-waiting web-first assertions, which retry until the condition is
true (or a timeout elapses) instead of checking visibility once at an arbitrary instant. Actions
that change page identity (login, save, logout) return the next page object, giving readable,
chainable test code:

```java
DashboardPage dashboard = loginPage().loginAs(username, password);
EmployeeListPage list = dashboard.sidebar.goToPim();
EmployeeDetailsPage details = list.openFirstEmployee();
```

Business assertions live in the tests, not the page objects — page objects only expose what a
real user could see or do.

## Browser vs. BrowserContext vs. Page

- **Browser**: one Chromium/Firefox process, launched once by `BrowserManager` and reused for
  every test in the run. Launching a browser process is expensive (hundreds of ms to seconds);
  paying that cost once instead of per-test is the main performance reason to use Playwright
  this way instead of the Selenium WebDriver-per-test model.
- **BrowserContext**: a lightweight, isolated "incognito-style" session — its own cookies,
  localStorage, cache and permissions. `TestLifecycleExtension` creates a **new context for
  every test method** and closes it in `afterTestExecution`. This is what actually gives tests
  isolation; a logout in one test can never leak into another test's session because they don't
  share a context. Creating a context costs milliseconds, unlike launching a browser.
- **Page**: a single tab inside a context. Each test gets exactly one, injected into the test
  instance by the extension.

This is why the framework does **not** use a Selenium-style `ThreadLocal<WebDriver>` singleton:
Playwright's context model already solves isolation at a cheaper layer, so there is nothing left
for a thread-local driver wrapper to do.

## Locator Strategy

Locators prioritize, in order: `getByRole`, `getByPlaceholder`, `getByLabel`-equivalent structural
filters, `getByText`. CSS class names are used only where OrangeHRM's own "oxd" design system
exposes a real, intentional class (e.g. `div.oxd-input-group` to scope a label+input pair) and no
semantic alternative exists — never auto-generated or hashed classes, and no XPath anywhere in the
framework.

One concrete example: the employee table in `EmployeeListPage` is rendered with proper ARIA roles
(`role="table"`, `role="row"`, `role="cell"`, `role="columnheader"`) even though it's built from
`<div>`s, not a real `<table>` element. This was confirmed by reading the live accessibility tree
before writing the locator, which is why data rows are targeted as
`getByRole(ROW).filter(has: getByRole(CELL))` — that reliably excludes the header row (whose
children are `columnheader`, not `cell`) without depending on any CSS class at all.

## Auto-Waiting (and why there is no `WaitUtils`)

Playwright locators re-resolve on every action and assertion and wait for the target to be
attached, visible, stable, and enabled before acting — this is what "actionability checks" means.
Combined with web-first assertions (`assertThat(locator).isVisible()`, which polls until true or
timeout), the vast majority of situations that would need an explicit wait in Selenium simply
don't need one here.

There are exactly two places in this framework with an explicit wait, both because Playwright
cannot infer the condition from the DOM alone:

1. `AddEmployeePage.save()` calls `page.waitForURL("**/viewPersonalDetails/**")` after clicking
   Save. The redirect is a client-side (Vue router) navigation, not a full page load, so
   Playwright's `click()` does not block on it the way it would for a real browser navigation —
   this was discovered as a real, reproducible race condition while building the framework (the
   test would sometimes read `page.url()` before the SPA had actually routed), not added
   speculatively.
2. `EmployeeListPage.searchByName()` uses `pressSequentially()` instead of `fill()` for the
   Employee Name field. That field is a typeahead whose suggestion dropdown is driven by a
   debounced keystroke listener; `fill()` sets the value directly and never fires the events the
   listener needs, so the dropdown never opens. This was also found empirically (via a throwaway
   diagnostic test that dumped the field's live DOM) rather than assumed.

No `Thread.sleep()` calls are used in the current source code; this was verified during the
pre-publish code review. There is no general-purpose `WaitUtils` class — every wait that exists
is one line, local to the one interaction that needs it.

## Tracing

`TestLifecycleExtension` starts tracing (`setScreenshots(true).setSnapshots(true).setSources(true)`)
at the beginning of every test's `BrowserContext`. On a **passing** test, tracing is stopped and
discarded — recording a trace for every green test would produce gigabytes of artifacts nobody
looks at. On a **failing** test, the trace is saved to `target/traces/<Class>_<method>.zip`.

To inspect a trace:

```bash
mvn -q exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="show-trace target/traces/<file>.zip"
```

or drag the `.zip` onto https://trace.playwright.dev/. The trace viewer gives a timeline of every
action, a DOM snapshot at each step, console logs, and network requests — the single biggest
practical difference from debugging a failed Selenium test.

## Screenshots on Failure

`FailureDiagnostics` (in `utils/`) is the **single owner** of failure-artifact capture — nothing
else in the framework takes a screenshot. On failure, `TestLifecycleExtension` calls it once:
a full-page PNG is saved to `target/screenshots/` and attached to the Allure result via
`Allure.addAttachment`. There is no second, competing screenshot mechanism, so a failed test
produces exactly one screenshot, not several duplicates from different layers.

## Allure

Allure results (JSON) are written to `target/allure-results` by the `allure-junit5` extension on
every run, and failure screenshots are attached to the relevant test result automatically.

**Verified so far**: `target/allure-results` is populated correctly after `mvn test` (confirmed
locally). **Not verified**: the HTML report itself — the Allure command-line tool is not installed
in this environment, so `allure generate`/`allure serve` has not been run. To view the report
after installing the [Allure CLI](https://allurereport.org/docs/install/):

```bash
allure serve target/allure-results
```

## Configuration

Resolution priority: **JVM system property > environment variable > `config.properties` > hardcoded
default.**

| Key | Default | Example override |
|---|---|---|
| `browser` | `chromium` | `-Dbrowser=firefox` |
| `headless` | `true` | `-Dheadless=false` |
| `baseUrl` | `https://opensource-demo.orangehrmlive.com/` | `-DbaseUrl=...` |

`ConfigManager` (framework config) and `TestDataManager` (login credentials) both implement this
same resolution order independently, since they load two different files for two different
purposes (runtime behavior vs. test input data) — see [Test Data](#test-data--the-shared-demo-environment).

## Test Data & the Shared Demo Environment

`opensource-demo.orangehrmlive.com` is a single public instance shared by testers worldwide, and
its employee list is constantly mutated by other people's scripts (over 150 employee records
exist at any given time, many clearly automation artifacts themselves). Two consequences shaped
the test design:

- Login credentials (`Admin` / `admin123`) are OrangeHRM's own published demo credentials, loaded
  from `src/test/resources/testdata/credentials.properties`, and overridable the same way as
  framework config.
- Tests that need "an existing employee" **create their own** with a timestamp-unique name
  (`TestDataManager.uniqueFirstName()`) rather than assuming any specific employee already exists
  — a name that was present yesterday may be gone or renamed by someone else's test run today.

## Test Scenarios (14)

**Authentication**
1. Valid login reaches the Dashboard
2. Invalid credentials show an error and stay on the login page
3. Empty username shows a required-field error
4. Empty password shows a required-field error

**Employee Management**
5. Employee list is displayed with results
6. Searching for an employee just added returns that employee
7. Searching for a nonexistent employee shows "No Records Found"
8. Opening an employee from the list navigates to their Personal Details
9. Adding a new employee succeeds and lands on their Personal Details page
10. Saving the Add Employee form with no name shows required-field errors

**Navigation & Session**
11. The Dashboard is displayed immediately after login
12. Each sidebar module link navigates to its own module
13. Logout returns the user to the login page
14. After logout, a protected page redirects back to the login page

## Project Structure

```
.
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .github/workflows/tests.yml
├── src/main/java/com/samvel/playwright/
│   ├── browser/BrowserManager.java
│   ├── components/SidebarComponent.java
│   ├── config/ConfigManager.java
│   ├── pages/  (LoginPage, DashboardPage, EmployeeListPage, AddEmployeePage, EmployeeDetailsPage)
│   └── utils/FailureDiagnostics.java
├── src/main/resources/config.properties
├── src/test/java/com/samvel/playwright/
│   ├── base/   (BaseTest, TestDataManager)
│   ├── listeners/TestLifecycleExtension.java
│   └── tests/  (LoginTests, EmployeeManagementTests, NavigationSessionTests)
└── src/test/resources/
    ├── allure.properties
    └── testdata/credentials.properties
```

## How to Run

### Prerequisites
- Java 17+, Maven 3.9+
- Playwright browsers installed once per machine:
  ```bash
  mvn -q exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps chromium firefox"
  ```

### Run locally (headless by default)
```bash
mvn test
```

### Run headed (watch the browser)
```bash
mvn test -Dheadless=false
```

### Select a browser
```bash
mvn test -Dbrowser=firefox -Dheadless=true
```

### Run with Docker
```bash
docker build -t playwright-java-automation-framework .
docker run --rm --shm-size=1gb playwright-java-automation-framework
```

or with Compose (also mounts `target/` back out to the host so results/screenshots/traces survive
the container):
```bash
docker compose up --build
```

### Inspect a trace from a failed test
```bash
mvn -q exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="show-trace target/traces/<file>.zip"
```

## Future Improvements

- Parallel test execution (JUnit 5 parallel execution + one BrowserContext per thread) once the
  suite is large enough that serial runtime becomes a problem.
- API-based test data setup (create/delete employees via OrangeHRM's REST endpoints) to make
  employee-management tests faster and fully independent of UI timing.
- WebKit coverage, if/when it proves stable enough in CI not to add flakiness.
- Visual regression checks on stable, low-traffic screens (the shared demo's constantly-changing
  employee data makes this impractical for the employee list itself).
