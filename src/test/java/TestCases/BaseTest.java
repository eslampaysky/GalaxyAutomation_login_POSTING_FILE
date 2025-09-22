package TestCases;

import Pages.LoginPage;
import Utilities.Selenium.DriverFactory;
import Utilities.TestData.UserRole;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

public class BaseTest {

    protected LoginPage loginPage;
    protected boolean loginRequired = true;
    protected UserRole userRole = UserRole.MERCHANT; // Default

    @BeforeMethod
    public void setUp() {
        if (loginRequired) {
            loginPage = new LoginPage();

            switch (userRole) {
                case ADMIN:
                    loginPage.Login_admin();
                    break;
                case MERCHANT:
                    loginPage.Login_merchant();
                    break;
            }
        }
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
