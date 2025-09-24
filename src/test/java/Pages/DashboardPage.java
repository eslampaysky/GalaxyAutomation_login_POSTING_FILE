package Pages;




import java.time.Duration;
import io.qameta.allure.Step;


import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import Utilities.Selenium.DriverFactory;


public class DashboardPage {

    protected WebDriver driver;
    WebDriverWait wait;

    public DashboardPage() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }
    
    public WebElement SettlementReportTab() {
        // Wait until the loader is not visible before clicking
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
         
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"MerchantSettlementTransactions\"]/a")));
    }
    
    public WebElement service_click() {
        // Wait until the loader is not visible before clicking
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));

        // Wait until the service link is clickable
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"MerchantSettlementTransactions\"]/a")));
    }
    public WebElement Exported_files_page() {
        // Wait until the loader is not visible before clicking
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
         
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"ExportedFilesDownloadSearchBase\"]/a/span")));
    }
    public void scrollToBottom(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        // Scroll to the bottom of the page
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Wait a moment to ensure the page scrolls and loads any new content
        try {
            Thread.sleep(2000); // Sleep for 2 seconds to let the page load
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

   @Step("Go to Settlement Report page")
    public void Go_to_Settlement_Page() {
        SettlementReportTab();
        service_click().click();
        
        }
    @Step("Go to Exported Files page")
    public void Go_to_Exported_files_page() {
         try {
            Thread.sleep(2000); // Sleep for 2 seconds to let the page load
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scrollToBottom(driver);
        Exported_files_page();
        Exported_files_page().click();
       
    }
       
    

}
