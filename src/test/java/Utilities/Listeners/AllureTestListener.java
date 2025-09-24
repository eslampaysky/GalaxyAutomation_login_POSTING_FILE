package Utilities.Listeners;

import Utilities.Selenium.DriverFactory;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;

public class AllureTestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            if (DriverFactory.getDriver() != null) {
                byte[] screenshot = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("📸 Screenshot on Failure", new ByteArrayInputStream(screenshot));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
