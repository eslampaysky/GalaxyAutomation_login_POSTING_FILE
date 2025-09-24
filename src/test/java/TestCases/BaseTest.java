package TestCases;

import Pages.LoginPage;
import Utilities.Selenium.DriverFactory;
import Utilities.TestData.UserRole;
import io.qameta.allure.Allure;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import Utilities.Listeners.AllureTestListener;
import io.qameta.allure.Step;

public class BaseTest {

    protected LoginPage loginPage;
    protected boolean loginRequired = true;
    protected UserRole userRole = UserRole.MERCHANT; // default role

    @BeforeMethod
    public void setUp() {
        if (loginRequired) {
            loginPage = new LoginPage(); // Driver is handled inside LoginPage

            // Login based on role
            switch (userRole) {
                case ADMIN:
                    Allure.step("Login as ADMIN");
                    loginPage.Login_admin();
                    break;
                case MERCHANT:
                    Allure.step("Login as MERCHANT");
                    loginPage.Login_merchant();
                    break;
            }
        }
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
        Allure.step("✅ Driver closed");
    }
}
