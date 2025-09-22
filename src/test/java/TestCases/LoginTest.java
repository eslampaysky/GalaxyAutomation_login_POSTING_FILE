package TestCases;


import io.qameta.allure.*;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;




@Epic("Authentication")
@Feature("Login Functionality")
public class LoginTest extends BaseTest {  // Extends BaseTest (login logic is reused)

    @Test(description = "Verify user can login with valid credentials.")
    @Severity(SeverityLevel.BLOCKER)
    @Story("User logs in with correct credentials")
    @Description("This test checks if a user can successfully log in and sends otp successfully.")
    public void loginWithValidData() {
        Allure.step("Check if OTP is visible in login page after login");
         loginPage.Login_admin();

        WebElement OTP = loginPage.successMsg();  // Provided by BaseTest

        Allure.step("Assert dashboard is displayed");
        Assert.assertTrue(OTP.isDisplayed(), "Dashboard element should be visible.");
    }
}
