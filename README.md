# Digital.ai Testing - Automated Accessibility Testing

Run automated accessibility scans on mobile devices using **Appium**,
**Digital.ai Testing** as a device farm, and the **Deque Axe** engine.

---

## What it does

- Opens web pages on real mobile devices in the Digital.ai Testing cloud.
- Runs Axe scans (`mobile: axeScan`) on each page.
- Writes a summary to the Digital.ai report.
---

## Prerequisites

| Requirement | Notes                                                                                             |
|---|---------------------------------------------------------------------------------------------------|
| **Java (JDK) 11 or newer** | JDK 17 (LTS) recommended. Required as the project is leveraging Appium `java-client` 9.         |
| **Digital.ai Testing account** | You need an **Access Key** and your cloud **URL** (e.g. `https://uscloud.experitest.com/wd/hub`). |
| **Axe API key** | Provided by Deque / Digital.ai for accessibility scanning.                                        |

---

## Setup

1. **Clone the repo.**

   ```bash
   git clone <repo-url>
   cd Accessibility-Testing-Powered-By-Deque
   ```

2. **Populate the following** in `src/test/resources/config.properties`:

   | Key | What to put |
   |---|---|
   | `axe.apiKey` | Your Axe API key. |
   | `dai.accessKey` | Your Digital.ai Testing access key. |
   | `dai.environment` | Your Digital.ai cloud URL. |
   | `deviceQuery` | Which device to run on (see [Choosing the device](#choosing-the-device)). |
   | `failTests` | `true` to fail tests on critical/serious issues, `false` to log only. |

---

## Dependencies & build

- The **Gradle wrapper** (`gradlew` / `gradlew.bat`) is included, so the first
  `./gradlew` run downloads the right Gradle version automatically. No manual
  Gradle install needed.
- **Dependency versions lives in `build.gradle`** (the `dependencies { }` block):
  TestNG, Appium `java-client`, gson, and so on. `./gradlew test` downloads and
  caches them on the first run.
- To change a version, edit `build.gradle` and re-run `./gradlew test`.

> The first run needs internet access to download Gradle and the dependencies.

---

## Running the tests

### Option A — IntelliJ

Right-click `src/test/resources/testng.xml` → **Run**.
This runs the suite.

### Option B — Gradle

```bash
./gradlew test
```

---

## Parallel execution

Parallelism is controlled by `src/test/resources/testng.xml`:

```xml
<suite name="AccessibilitySuite" parallel="methods" thread-count="3">
```

- `parallel="methods"` — each test method runs on its own thread.
- `thread-count` — how many run at the same time.

Each test gets its own device and its own driver session — sessions are kept
separate per thread, so parallel runs don't interfere with each other.

---

## Choosing the device

The device is set by the `deviceQuery` value in `config.properties` — no code
change needed:

```properties
deviceQuery=@os='ios' and contains(@name, 'iPhone 15')
```

Change it to match the devices in your cloud. A few examples:

| Goal | Query |
|---|---|
| Any iOS device | `@os='ios'` |
| A device by name | `@os='ios' and contains(@name, 'iPhone 15')` |
| A specific device | `@serialNumber='...'` |

---

## Project structure

```
src/test/
├── java/
│   ├── tests/
│   │   └── FunctionalAndAccessibilityTests.java   # the example test suite
│   └── utils/
│       ├── ConfigReader.java                      # reads config.properties
│       └── TestHelpers.java                       # reusable scan + report helpers
└── resources/
    ├── config.properties                          # your keys + settings (fill these in)
    └── testng.xml                                 # suite + parallel settings
```

### `FunctionalAndAccessibilityTests`
Each `@Test` opens a page, scans it in a few states, and
checks the page loaded. Use it as the template for your own pages.

### `TestHelpers`
Stateless helpers shared across tests. All methods are static and take the
driver they need, so they are safe to call from parallel tests:

| Method | Purpose |
|---|---|
| `runAxeScan(driver, scanName)` | Run an Axe scan, write results to the report. |
| `waitForPageReady(wait)` | Wait until the page finishes loading. |
| `scrollDown(driver)` / `scrollToTop(driver)` | Scroll the page to scan different states. |
| `setReportStatus(driver, result)` | Set the pass/fail/skip status in the report. |
| `addPropertyToTestReport(driver, name, value)` | Add a single property to the report. |

### `ConfigReader`
Reads values from `config.properties`. Use it anywhere:

```java
String accessKey = ConfigReader.get("dai.accessKey");
```

---

## Reading the report

For each scan the report shows:

- A summary line, e.g. `Total Issues Scan: 50 | Critical: 11 | Serious: 21 | Moderate: 16 | Minor: 2`.
- A **has_ada_issue** flag — `true` if the page has any issue, `false` if clean.

---

## Documentation

For documentation, head to the [Digital.ai Testing Docs Page](https://docs.digital.ai/continuous-testing/).
