package utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TestHelpers {

    private TestHelpers() {}

    public static void waitForPageReady(WebDriverWait wait) {
        wait.until(webDriver ->
                ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")
                        .equals("complete")
        );
    }

    public static void scrollDown(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, window.innerHeight * 0.8);");
        sleep(1000);
    }

    public static void scrollToTop(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        sleep(1000);
    }

    public static void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // --- DAI reporter helpers ---

    /** Adds a single key/value property to the DAI test report. */
    public static void addPropertyToTestReport(WebDriver driver, String name, Object value) {
        ((JavascriptExecutor) driver).executeScript("seetest:client.addTestProperty", name, value);
    }

    /** Maps a TestNG result to the matching DAI report status (passed/failed/skipped). */
    public static void setReportStatus(WebDriver driver, ITestResult result) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        if (result.getStatus() == ITestResult.SUCCESS) {
            js.executeScript("seetest:client.setReportStatus", "Passed", "Test Passed");
        } else if (result.getStatus() == ITestResult.FAILURE) {
            String failureReason = result.getThrowable() != null
                    ? result.getThrowable().toString()
                    : "Test failed without an exception message";
            js.executeScript("seetest:client.setReportStatus", "Failed", "Test Failed", failureReason);
        } else if (result.getStatus() == ITestResult.SKIP) {
            String skipReason = result.getThrowable() != null
                    ? result.getThrowable().toString()
                    : "Test skipped";
            js.executeScript("seetest:client.setReportStatus", "Skipped", "Test Skipped", skipReason);
        }
    }

    /**
     * Runs a "mobile: axeScan", writes report metadata, and (when failTests=true in config)
     * fails the test on critical/serious issues.
     */
    @SuppressWarnings("unchecked")
    public static void runAxeScan(WebDriver driver, String scanName) {
        Map<String, Object> params = new HashMap<>();
        params.put("apiKey", ConfigReader.get("axe.apiKey"));
        params.put("scanName", scanName);

        Map<String, Object> result =
                (Map<String, Object>) ((JavascriptExecutor) driver).executeScript("mobile: axeScan", params);

        int critical = 0;
        int serious = 0;
        int moderate = 0;
        int minor = 0;

        List<Map<String, Object>> ruleResults =
                (List<Map<String, Object>>) result.get("axeRuleResults");

        for (Map<String, Object> rule : ruleResults) {

            // Only count actual failed accessibility checks
            if (!"FAIL".equalsIgnoreCase(String.valueOf(rule.get("status")))) {
                continue;
            }

            int impact = ((Number) rule.get("impact")).intValue();

            if (impact == 3) {
                critical++;
            } else if (impact == 2) {
                serious++;
            } else if (impact == 1) {
                moderate++;
            } else if (impact == 0) {
                minor++;
            }
        }

        int totalIssues = critical + serious + moderate + minor;

        String summary =
                "Total Issues Scan: " + totalIssues +
                        " | Critical: " + critical +
                        " | Serious: " + serious +
                        " | Moderate: " + moderate +
                        " | Minor: " + minor;

        boolean hasAdaIssue = totalIssues > 0;
        addPropertyToTestReport(driver, "has_ada_issue", hasAdaIssue);

        boolean accessibilityPassed = critical == 0 && serious == 0;
        boolean failTests = Boolean.parseBoolean(ConfigReader.get("failTests"));

        // When failTests=false, always report passed so the scan is logged, not enforced.
        boolean reportPassed = !failTests || accessibilityPassed;

        ((JavascriptExecutor) driver).executeScript(
                "seetest:client.report",
                summary,
                String.valueOf(reportPassed)
        );

        System.out.println("Axe scan completed: " + scanName);

        if (failTests && !accessibilityPassed) {
            Assert.fail(summary);
        }
    }
}
