package TestCases;

import Pages.*;
import Utilities.Selenium.DriverFactory;
import io.qameta.allure.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import Utilities.Listeners.AllureTestListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Epic("Reporting")
@Feature("Settlement Report Download & Validation")
@Owner("Eslam Samy")
@Listeners(AllureTestListener.class)
public class SettlementReportTest {

    WebDriver driver;
    WebDriverWait wait;
    LoginPage loginPage;
    DashboardPage dashboardPage;
    SettlementReportPage settlementPage;
    ExportedFilesPage exportPage;

    private static final String DOWNLOAD_PATH = "C:\\Users\\EslamSamy\\Downloads\\";
    private static final String PYTHON_SCRIPT =
            "C:\\Users\\EslamSamy\\IdeaProjects\\Galaxy_Auto_Login_Test\\split_text_with_labels.py";

    @BeforeMethod
    @Step("Setup WebDriver and clean old downloaded files")
    public void setUp() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(50));
        loginPage = new LoginPage();
        dashboardPage = new DashboardPage();
        settlementPage = new SettlementReportPage();
        exportPage = new ExportedFilesPage();

        // Clean old .zip files
        File downloadDir = new File(DOWNLOAD_PATH);
        File[] files = downloadDir.listFiles((dir, name) -> name.endsWith(".zip"));
        if (files != null) {
            for (File file : files) {
                file.delete();
                Allure.step("Deleted old file: " + file.getName());
            }
        }
    }

    @Test(groups = {"critical"})
    @Severity(SeverityLevel.CRITICAL)
    @Story("Admin downloads and validates settlement report")
    @Description("Test downloads a settlement report, verifies file correctness, and validates against DB using Python scripts.")
    public void downloadSettlementReport() throws Exception {
        // Step 1: Login
        Allure.step("Step 1: Login as admin");
        loginPage.Login_admin();
        Allure.step("✅ Logged in successfully");

        // Step 2: Navigate to Settlement page
        Allure.step("Step 2: Navigate to Settlement page");
        dashboardPage.Go_to_Settlement_Page();
        Allure.step("✅ Navigated to Settlement page");

        // Step 3: Download settlement report
        Allure.step("Step 3: Download settlement report");
        settlementPage.Download_Settlement_Report();
        WebElement success_msg = settlementPage.sucess_download_message();
        Assert.assertTrue(success_msg.isDisplayed(), "❌ Success message not displayed after report download.");
        Allure.step("✅ Settlement report requested successfully");

        // Step 4: Go to Exported Files page
        Allure.step("Step 4: Navigate to Exported Files page");
        dashboardPage.Go_to_Exported_files_page();
        Allure.step("✅ Navigated to Exported Files page");

        // Step 5: Download the exported settlement report
        Allure.step("Step 5: Download exported settlement report");
        String exportedTimestamp = exportPage.Download_Settlement_Report_Device();
        Allure.step("📌 Extracted timestamp from Exported Files: " + exportedTimestamp);

        // Step 6: Verify downloaded file name contains the timestamp
        Allure.step("Step 6: Verify downloaded file name");
        boolean isCorrect = exportPage.verifyAndExtractFile(exportedTimestamp);
        Assert.assertTrue(isCorrect, "❌ Downloaded file name does not match exported timestamp.");
        Allure.step("✅ Settlement report file downloaded and verified successfully");

        // Step 7: Call Python script with timestamp ONLY
        Allure.step("Step 7: Run Python script with report timestamp");
        runPythonScript(exportedTimestamp + ".txt");

        // Step 7b: Validate against DB
        Allure.step("Step 7b: Validate report data against DB");
        String dateFrom = "2025-08-01 00:00:00";
        String dateTo = "2025-08-20 23:59:59";
        runPythonScript("validate_with_db.py", exportedTimestamp, dateFrom, dateTo);
    }

    private void runPythonScript(String... args) {
        try {
            String pythonExe = "python";

            ProcessBuilder pb;
            if (args.length == 1) {
                pb = new ProcessBuilder(pythonExe, PYTHON_SCRIPT, args[0]);
            } else {
                pb = new ProcessBuilder();
                pb.command().add(pythonExe);
                for (String arg : args) pb.command().add(arg);
            }

            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder fullOutput = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                fullOutput.append(line).append("\n");

                // Mark passes and fails clearly in Allure
                if (line.contains("[FAIL]")) {
                    Allure.step("❌ " + line);
                } else if (line.contains("[PASS]")) {
                    Allure.step("✅ " + line);
                } else {
                    Allure.step("📌 " + line);
                }
                System.out.println("[PYTHON] " + line);
            }

            int exitCode = process.waitFor();
            Assert.assertEquals(exitCode, 0, "❌ Python script failed!");

            // Attach full output
            Allure.addAttachment("Python Script Output", "text/plain",
                    fullOutput.toString(), ".txt");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("❌ Failed to execute Python script!");
        }
    }

    @AfterMethod
    @Step("Close WebDriver")
    public void tearDown() {
        DriverFactory.quitDriver();
        Allure.step("✅ WebDriver closed");
    }
}
