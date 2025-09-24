package TestCases;

import io.qameta.allure.*;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.Listeners;
import Utilities.Listeners.AllureTestListener;
import Pages.LoginPage;
import io.qameta.allure.Step;


@Epic("Authentication")
@Feature("Login Functionality")
@Owner("Eslam Samy")
@Listeners(AllureTestListener.class)
public class LoginTest extends BaseTest {

    @Test(groups = {"critical"})
    @Severity(SeverityLevel.BLOCKER)
    @Story("User logs in with correct credentials")
    @Description("Verify that a user can successfully log in as Merchant and OTP is visible after login.")
    @Link(name = "Login Page Spec", url = "https://your-tracking-tool/login-spec") // optional
    public void loginWithValidData() {
        Allure.step("Login as merchant");
        loginPage.Login_merchant();

        Allure.step("Check if OTP is visible after login");
        WebElement OTP = loginPage.successMsg();

        Allure.step("Assert OTP message element is displayed");
        Assert.assertTrue(OTP.isDisplayed(), "OTP Message element should be visible.");
    }
}
