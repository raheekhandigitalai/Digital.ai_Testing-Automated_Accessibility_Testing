package tests;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileBrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import utils.ConfigReader;
import utils.TestHelpers;

import static org.testng.Assert.assertTrue;

public class FunctionalAndAccessibilityTests {

    private static final String HOME_PAGE_URL = "https://www.w3.org/WAI/demos/bad/before/home.html";
    private static final String NEWS_PAGE_URL = "https://www.w3.org/WAI/demos/bad/before/news.html";
    private static final String TICKETS_PAGE_URL = "https://www.w3.org/WAI/demos/bad/before/tickets.html";

    private final ThreadLocal<IOSDriver> driverThread = new ThreadLocal<>();
    private final ThreadLocal<WebDriverWait> waitThread = new ThreadLocal<>();

    private IOSDriver driver() {
        return driverThread.get();
    }

    private WebDriverWait getWait() {
        return waitThread.get();
    }

    @BeforeMethod
    public void setUp(Method method) throws MalformedURLException {
        DesiredCapabilities dc = new DesiredCapabilities();
        dc.setCapability("digitalai:testName", method.getName());
        dc.setCapability("digitalai:accessKey", ConfigReader.get("dai.accessKey"));
        dc.setCapability("digitalai:deviceQuery", ConfigReader.get("deviceQuery"));
        dc.setCapability("digitalai:appiumVersion", "3.4.2");
        dc.setBrowserName(MobileBrowserType.SAFARI);
        dc.setCapability("appium:automationName", "AxeXCUITest");

        IOSDriver driver = new IOSDriver(new URL(ConfigReader.get("dai.environment")), dc);
        driverThread.set(driver);
        waitThread.set(new WebDriverWait(driver, Duration.ofSeconds(10)));
    }

    @Test
    public void home_page_test() {
        driver().navigate().to(HOME_PAGE_URL);

        TestHelpers.waitForPageReady(getWait());
        TestHelpers.runAxeScan(driver(),"BAD Site - Home Page - Initial Load");

        TestHelpers.scrollDown(driver());
        TestHelpers.runAxeScan(driver(),"BAD Site - Home Page - After Scroll Down");

        TestHelpers.scrollToTop(driver());
        TestHelpers.runAxeScan(driver(),"BAD Site - Home Page - After Scroll Back To Top");

        assertTrue(driver().getTitle().toLowerCase().contains("citylights"));
    }

    @Test
    public void news_page_test() {
        driver().get(NEWS_PAGE_URL);

        TestHelpers.waitForPageReady(getWait());
        TestHelpers.runAxeScan(driver(),"BAD Site - News Page - Initial Load");

        TestHelpers.scrollDown(driver());
        TestHelpers.runAxeScan(driver(),"BAD Site - News Page - After Scroll Down");

        TestHelpers.scrollDown(driver());
        TestHelpers.runAxeScan(driver(),"BAD Site - News Page - Further Down Page");

        assertTrue(driver().getCurrentUrl().contains("news.html"));
    }

    @Test
    public void tickets_page_test() {
        driver().get(TICKETS_PAGE_URL);

        TestHelpers.waitForPageReady(getWait());
        TestHelpers.runAxeScan(driver(),"BAD Site - Tickets Page - Initial Load");

        TestHelpers.scrollDown(driver());
        TestHelpers.runAxeScan(driver(),"BAD Site - Tickets Page - Tables Visible");

        assertTrue(driver().getCurrentUrl().contains("tickets.html"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver() == null) {
            return;
        }
        try {
            TestHelpers.setReportStatus(driver(), result);

            Object reportUrl = driver().getCapabilities()
                    .getCapability("digitalai:reportUrl");

            System.out.println("Report URL: " + reportUrl);

        } catch (Exception e) {
            System.err.println("Unable to update test report status: " + e.getMessage());

        } finally {
            driver().quit();
            driverThread.remove();
            waitThread.remove();
        }
    }
}