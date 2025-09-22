package TestCases;

import io.qameta.allure.*;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import Pages.LoginPage;

@Epic("Authentication")
@Feature("Login Functionality")
public class LoginTest extends BaseTest {

    @Test(groups = {"critical"})
    @Severity(SeverityLevel.BLOCKER)
    @Story("User logs in with correct credentials")
    @Description("This test checks if a user can successfully log in and OTP is visible.")
public void loginWithValidData() {
    if (loginPage == null) {
        loginPage = new LoginPage();
        loginPage.Login_merchant();
    }

    Allure.step("Check if OTP is visible in login page after login");
    WebElement OTP = loginPage.successMsg();

    Allure.step("Assert dashboard is displayed");
    Assert.assertTrue(OTP.isDisplayed(), "Dashboard element should be visible.");
}

}
