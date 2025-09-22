package Pages;

import Utilities.Selenium.DriverFactory;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import Utilities.TestData.Constant;
import java.time.Duration;

public class LoginPage {

    protected WebDriver driver;
    WebDriverWait wait;

    public LoginPage() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(50));
    }

    // Locators & actions
    public void openLoginPage() {
        driver.get(Constant.Portal_link);
    }

    public WebElement UserName() {
        return driver.findElement(By.cssSelector("#UserName"));
    }

    public WebElement Password() {
        return driver.findElement(By.cssSelector("#userpassword"));
    }

    public WebElement SendOtp() {
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[text()='Send OTP']")));
    }

    public WebElement successMsg() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".text-success")));
    }

    public WebElement Otp() {
        return wait.until(ExpectedConditions.elementToBeClickable(By.id("otp")));
    }

    public WebElement SignIn() {
        return driver.findElement(By.cssSelector(".sign-in-btn"));
    }

    public WebElement Dashboard() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#IndexMerchant > a > img")));
    }

    // Login logic
    public void Login_merchant() {
        openLoginPage();
        UserName().sendKeys(Constant.USERNAME);
        Password().sendKeys(Constant.PASSWORD);
        SendOtp().click();
        successMsg();
        }
        public void Login_admin() {
        openLoginPage();
        UserName().sendKeys(Constant.system_admin);
        Password().sendKeys(Constant.system_password);
        SendOtp().click();
        successMsg();
        Otp().sendKeys(Constant.OTP);
        SignIn().click();
    }
    

}
