package Pages;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import io.qameta.allure.Step;


import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import Utilities.Selenium.DriverFactory;

public class ExportedFilesPage {

    protected WebDriver driver;
    WebDriverWait wait;

    // 🔹 Always download to project folder (not Windows Downloads)
    private static final String PROJECT_DOWNLOAD_PATH =
            "C:\\Users\\EslamSamy\\Downloads\\";
    private static final String PROJECT_EXTRACT_PATH =
            "C:\\Users\\EslamSamy\\IdeaProjects\\Galaxy_Auto_Login_Test\\extracted\\";

    public ExportedFilesPage() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Ensure dirs exist
        new File(PROJECT_DOWNLOAD_PATH).mkdirs();
        new File(PROJECT_EXTRACT_PATH).mkdirs();
    }

    @Step("Get first exported file name element")
    public WebElement fileNameElement() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@id='divListPanel']/div/div[1]//td[@data-name='ExportFileName']")));
    }

    @Step("Extract timestamp from file name")
    public String getFileTimestamp() {
        String fileName = fileNameElement().getText();
        Pattern pattern = Pattern.compile("(\\d{14})"); // matches 14 digits
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            String timestamp = matcher.group(1);
            System.out.println("📌 Extracted timestamp: " + timestamp);
            return timestamp;
        } else {
            throw new RuntimeException("❌ Timestamp not found in file name: " + fileName);
        }
    }

    @Step("Click the download button")
    public WebElement downloadButton() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#page-loader > div")));
        return wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='divListPanel']/div/div[1]//a[contains(@class,'download')]")));
    }

    @Step("Download settlement report from Exported Files")
    public String Download_Settlement_Report_Device() {

        try {
            Thread.sleep(2000); // Sleep for 2 seconds to let the page load
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String exportedTimestamp = getFileTimestamp();

        // Delete old zip before new download
        cleanOldDownloads();

        downloadButton().click();

        try {
            Thread.sleep(2000); // wait for download to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return exportedTimestamp;
        
    }

    @Step("Verify downloaded file contains timestamp: {expectedTimestamp} and extract")
    public boolean verifyAndExtractFile(String expectedTimestamp) {
        try {
            Thread.sleep(3000); // Sleep for 3 seconds to let the page load
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        File dir = new File(PROJECT_DOWNLOAD_PATH);

        // Wait up to 15 sec for file to appear
        long end = System.currentTimeMillis() + 15000;
        File downloadedFile = null;

        while (System.currentTimeMillis() < end) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".zip"));
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getName().contains(expectedTimestamp)) {
                        downloadedFile = file;
                        break;
                    }
                }
            }
            if (downloadedFile != null) break;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }

        if (downloadedFile == null) {
            System.out.println("❌ No downloaded file found with timestamp: " + expectedTimestamp);
            return false;
        } else {
            System.out.println("✅ File downloaded: " + downloadedFile.getName());

            // Extract zip (overwrite old)
            extractZip(downloadedFile, PROJECT_EXTRACT_PATH);
            return true;
        }
    }

    private void cleanOldDownloads() {
        File downloadDir = new File(PROJECT_DOWNLOAD_PATH);
        File[] files = downloadDir.listFiles((dir, name) -> name.endsWith(".zip"));
        if (files != null) {
            for (File file : files) file.delete();
        }
    }

    private void extractZip(File zipFile, String destDir) {
        try {
            // Clean extract dir before extracting
            Files.walk(Paths.get(destDir))
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2)) // delete children first
                    .forEach(File::delete);

            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            System.out.println("✅ Extracted zip to: " + destDir);

        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to extract zip: " + zipFile.getName(), e);
        }
    }
}
