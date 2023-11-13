package com.cst438;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemTestGradebookService {

    public static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver-win64/chromedriver.exe";
    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 1000; // 1 second.
    WebDriver driver;

    @BeforeEach
    public void testSetup() throws Exception {
        // if you are not using Chrome,
        // the following lines will be different.
        System.setProperty(
                "webdriver.chrome.driver",
                CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);

        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);
    }

    @Test
    @Order(1)
    public void addAssignment() throws Exception  {
        // click on add assignment button/link
        WebElement addAssignmentLink = driver.findElement(By.xpath("//a[@href='/addAssignment']"));
        // go to add assignment page
        addAssignmentLink.click();
        Thread.sleep(SLEEP_DURATION);

        // find input elements
        WebElement assignmentName = driver.findElement(By.name("assignmentName"));
        WebElement dueDate = driver.findElement(By.name("dueDate"));
        WebElement courseId = driver.findElement(By.name("courseId"));

        // fill out input elements
        assignmentName.sendKeys("Selenium Test Assignment");
        dueDate.sendKeys("2023-10-19");
        courseId.sendKeys("40443");

        // submit 'add assignments'
        WebElement submitAddAssignment = driver.findElement(By.id("sassignment"));
        submitAddAssignment.click();
        Thread.sleep(SLEEP_DURATION);

        // go back to list view
        WebElement returnToListLink = driver.findElement(By.xpath("//a[@href='/']"));
        returnToListLink.click();
        Thread.sleep(SLEEP_DURATION);

        // check that values have been properly added
        List<WebElement> newAssignment = driver.findElements(By.xpath("//tr[last()]//td"));
        assertThat(newAssignment.get(0).getText()).isEqualTo("Selenium Test Assignment");
        assertThat(newAssignment.get(1).getText()).isEqualTo("BUS 370");
        assertThat(newAssignment.get(2).getText()).isEqualTo("2023-10-19");
    }

    @Test
    @Order(2)
    public void updateAssignment() throws Exception {
        // get list of assignments
        List<WebElement> assignmentList = driver.findElements(By.xpath("//tr[last()]//td"));

        // click on edit assignment link
        WebElement editAssignmentLink = driver.findElement(By.xpath("//tr[last()]//td[5]"));

        // go to edit assignment page
        editAssignmentLink.click();
        Thread.sleep(SLEEP_DURATION);

        // find input elements
        WebElement assignmentName = driver.findElement(By.name("assignmentName"));
        WebElement dueDate = driver.findElement(By.name("dueDate"));

        // change input elements
        assignmentName.clear();
        assignmentName.sendKeys("Selenium Test Update Assignment");
        dueDate.clear();
        dueDate.sendKeys("1999-10-19");

        // submit 'update assignment'
        WebElement submitAddAssignment = driver.findElement(By.id("uassignment"));
        submitAddAssignment.click();
        Thread.sleep(SLEEP_DURATION);

        // go back to list view
        WebElement returnToListLink = driver.findElement(By.xpath("//a[@href='/']"));
        returnToListLink.click();
        Thread.sleep(SLEEP_DURATION);

        // check that values have been properly updated
        List<WebElement> newAssignment = driver.findElements(By.xpath("//tr[last()]//td"));
        assertThat(newAssignment.get(0).getText()).isEqualTo("Selenium Test Update Assignment");
        assertThat(newAssignment.get(1).getText()).isEqualTo("BUS 370");
        assertThat(newAssignment.get(2).getText()).isEqualTo("1999-10-19");
    }

    @Test
    @Order(3)
    public void deleteAssignment() throws Exception {
        // get list of assignments
        List<WebElement> assignmentList = driver.findElements(By.xpath("//tr[last()]//td"));

        // click on delete assignment link
        WebElement deleteAssignmentLink = driver.findElement(By.xpath("//tr[last()]//td[6]"));

        // go to edit assignment page
        deleteAssignmentLink.click();
        Thread.sleep(SLEEP_DURATION);

        // submit 'delete' assignment
        WebElement submitDeleteAssignment = driver.findElement(By.id("dassignment"));
        submitDeleteAssignment.click();
        Thread.sleep(SLEEP_DURATION);

        // go back to list view
        WebElement returnToListLink = driver.findElement(By.xpath("//a[@href='/']"));
        returnToListLink.click();
        Thread.sleep(SLEEP_DURATION);

        // check that assignment is deleted
        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.xpath("//tr[td='Selenium Test Update Assignment']"));
        });
    }

    @AfterEach
    public void cleanup() {
        if (driver!=null) {
            driver.close();
            driver.quit();
            driver=null;
        }
    }
}