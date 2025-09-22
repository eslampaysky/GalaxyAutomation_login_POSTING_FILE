package Utilities.Selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class DriverFactory {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            driver.set(chromeDriverConfig());
        }
        return driver.get();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }

    private static WebDriver chromeDriverConfig() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        // Detect CI environment
        String ciEnv = System.getenv("GITHUB_ACTIONS");
        String ciProp = System.getProperty("ci");
        if ("true".equalsIgnoreCase(ciEnv) || "true".equalsIgnoreCase(ciProp)) {
            options.addArguments("--headless=new"); // New headless mode
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
        } else {
            options.addArguments("--start-maximized");
        }

        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

        WebDriver driverInstance = new ChromeDriver(options);

        long implicitWait = Long.parseLong(System.getProperty("implicitWait", "10"));
        driverInstance.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driverInstance.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        return driverInstance;
    }
}
