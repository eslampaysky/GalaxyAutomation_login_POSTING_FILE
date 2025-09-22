package Pages;

import java.time.Duration;
// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import Utilities.Selenium.DriverFactory;

public class SettlementReportPage {

    protected WebDriver driver;
    WebDriverWait wait;

    // private static final DateTimeFormatter FORMATTER =
    //         DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public SettlementReportPage() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(50));
    }

    public WebElement bank_drop_down() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
        return wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[@class='select2-selection select2-selection--single']")));
    }

    public WebElement search_drop_down() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("/html/body/span[2]/span/span[1]/input")));
    }

    public WebElement select_bank() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
        return wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[text()= 'ecommerce OFFUS bank']")));
    }

    public WebElement settlement_type_drop_down() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
        return wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id=\"frmSettlement\"]/div[2]/div/div/div/div[1]/div[2]/span/span[1]")));
    }

    public WebElement select_type() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
        return wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[text()= 'PostingFile']")));
    }

    // 🔹 Dynamic DateFrom = Today - 29 days
   
public void changeDateFrom() {
    // LocalDate fromDate = LocalDate.now().minusDays(29);
    // String formatted = fromDate.atStartOfDay().format(FORMATTER);
    String formatted = "08/01/2025 11:07:00";
    WebElement dateField = wait.until(ExpectedConditions.elementToBeClickable(By.id("DateFrom")));
    dateField.sendKeys(Keys.CONTROL + "a"); // select all
    dateField.sendKeys(Keys.DELETE);        // clear
    dateField.sendKeys(formatted);          // type new date
    // dateField.sendKeys(Keys.ENTER);         // confirm
}

// 🔹 Dynamic DateTo = Today

public void changeDateTo() {
    // LocalDate toDate = LocalDate.now();
    // String formatted = toDate.atTime(23, 59, 59).format(FORMATTER);
    String formatted = "08/20/2025 11:07:00";
    WebElement dateField = wait.until(ExpectedConditions.elementToBeClickable(By.id("DateTo")));
    dateField.sendKeys(Keys.CONTROL + "a"); // select all
    dateField.sendKeys(Keys.DELETE);        // clear
    dateField.sendKeys(formatted);          // type new date
    // dateField.sendKeys(Keys.ENTER);         // confirm
}


    public WebElement download_button() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
        return wait.until(ExpectedConditions.elementToBeClickable(By.id("btnDownload")));
    }

    public WebElement sucess_download_message() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("/html/body/div[2]/div/div/section/div[1]/div[1]/div[2]/div/p")));
    }

    public void Download_Settlement_Report() {
        bank_drop_down().click();
        search_drop_down().sendKeys("OFFUS");
        select_bank().click();

        settlement_type_drop_down().click();
        select_type().click();
        settlement_type_drop_down().click();
        // changeDateTo();
        //  try {
        //     Thread.sleep(2000); // Sleep for 2 seconds to let the page load
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
        changeDateFrom();
        
         try {
            Thread.sleep(2000); // Sleep for 2 seconds to let the page load
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        changeDateTo();
         try {
            Thread.sleep(2000); // Sleep for 2 seconds to let the page load
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        
        // select_type().click();

        download_button().click();
        try {
            Thread.sleep(2000); // Sleep for 2 seconds to let the page load
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
