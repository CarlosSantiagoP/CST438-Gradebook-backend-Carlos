package com.cst438;

import static org.assertj.core.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.test.context.SpringBootTest;

/*
 * This example shows how to use selenium testing using the web driver 
 * with Chrome browser.
 * 
 *  - Buttons, input, and anchor elements are located using XPATH expression.
 *  - onClick( ) method is used with buttons and anchor tags.
 *  - Input fields are located and sendKeys( ) method is used to enter test data.
 *  - Spring Boot JPA is used to initialize, verify and reset the database before
 *      and after testing.
 *      
 *  In SpringBootTest environment, the test program may use Spring repositories to 
 *  setup the database for the test and to verify the result.
 */

@SpringBootTest
public class EndToEndTestSubmitGrades {

	public static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver_win32/chromedriver.exe";

	public static final String URL = "http://localhost:3000";
	public static final int SLEEP_DURATION = 1000; // 1 second.
	public static final String TEST_ASSIGNMENT_NAME = "db design";
	public static final String NEW_GRADE = "99";


	@Test
	public void addCourseTest() throws Exception {



		// set the driver location and start driver
		//@formatter:off
		// browser	property name 				Java Driver Class
		// edge 	webdriver.edge.driver 		EdgeDriver
		// FireFox 	webdriver.firefox.driver 	FirefoxDriver
		// IE 		webdriver.ie.driver 		InternetExplorerDriver
		//@formatter:on

		/*
		 * initialize the WebDriver and get the home page. 
		 */

		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
		WebDriver driver = new ChromeDriver();
		// Puts an Implicit wait for 10 seconds before throwing exception
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		driver.get(URL);
		Thread.sleep(SLEEP_DURATION);
		
		WebElement w;
		

		try {
			/*
			* locate the <td> element for assignment title 'db design'
			*
			*/
			
			List<WebElement> elements  = driver.findElements(By.xpath("//td"));
			boolean found = false;
			for (WebElement we : elements) {
				if (we.getText().equals(TEST_ASSIGNMENT_NAME)) {
					found=true;
					we.findElement(By.xpath("..//a")).click();
					break;
				}
			}
			assertThat(found).withFailMessage("The test assignment was not found.").isTrue();

			/*
			 *  Locate and click Grade button to indicate to grade this assignment.
			 */
			
			Thread.sleep(SLEEP_DURATION);

			/*
			 *  enter grades for all students, then click save.
			 */
			ArrayList<String> originalGrades = new ArrayList<>();
			elements  = driver.findElements(By.xpath("//input"));
			for (WebElement element : elements) {
				originalGrades.add(element.getAttribute("value"));
				element.clear();
				element.sendKeys(NEW_GRADE);
				Thread.sleep(SLEEP_DURATION);
			}
			
			for (String s : originalGrades) {
				System.out.println("'"+s+"'");
			}

			/*
			 *  Locate submit button and click
			 */
			driver.findElement(By.id("sgrade")).click();
			Thread.sleep(SLEEP_DURATION);
			
			w = driver.findElement(By.id("gmessage"));
			assertThat(w.getText()).withFailMessage("After saving grades, message should be \"Grades saved.\"").startsWith("Grades saved");
			
			driver.navigate().back();  // back button to last page
			Thread.sleep(SLEEP_DURATION);
			
			// find the assignment 'db design' again.
			elements  = driver.findElements(By.xpath("//td"));
			found = false;
			for (WebElement we : elements) {
				if (we.getText().equals(TEST_ASSIGNMENT_NAME)) {
					found=true;
					we.findElement(By.xpath("..//a")).click();
					break;
				}
			}
			Thread.sleep(SLEEP_DURATION);
			assertThat(found).withFailMessage("The test assignment was not found.").isTrue();
			
			// verify the grades. Change grades back to original values

			elements  = driver.findElements(By.xpath("//input"));
			for (int idx=0; idx < elements.size(); idx++) {
				WebElement element = elements.get(idx);
				assertThat(element.getAttribute("value")).withFailMessage("Incorrect grade value.").isEqualTo(NEW_GRADE);
				
				// clear the input value by backspacing over the value
				while(!element.getAttribute("value").equals("")){
			        element.sendKeys(Keys.BACK_SPACE);
			    }
				if (!originalGrades.get(idx).equals("")) element.sendKeys(originalGrades.get(idx));
				Thread.sleep(SLEEP_DURATION);
			}
			driver.findElement(By.id("sgrade")).click();
			Thread.sleep(SLEEP_DURATION);
			
			w = driver.findElement(By.id("gmessage"));
			assertThat(w.getText()).withFailMessage("After saving grades, message should be \"Grades saved.\"").startsWith("Grades saved");


		} catch (Exception ex) {
			throw ex;
		} finally {

			driver.quit();
		}

	}

	@Test
	public void updateGradeTest() throws Exception {
		try {
			WebElement gradeInput = Thread.findElement(By.id("grade-input-id")); // Replace with the actual ID

//			Delete the current values
			gradeInput.clear();
			gradeInput.sendKeys("NewGrade");

//			Find the button
			Thread.findElement(By.id("update-button-id")).click(); // Replace with the actual ID

//			Check for update
			WebElement messageElement = Thread.findElement(By.id("Grade has been updated"));
			assertThat(messageElement.getText()).isEqualTo("Grade updated successfully");

		} catch (Exception e) {
			throw e;
		}finally {
			Thread.quit(); //
		}

	}

	@Test
	public void deleteGradeTest() throws Exception {
		// Initialize the WebDriver and navigate to the page

		try {
			// Locate the assignment you want to delete (similar to your existing code)
			// Click on the assignment to open it

			// Find the delete button and click it
			Thread.findElement(By.id("delete-button-id")).click(); // Replace with the actual ID

			// Handle any confirmation dialog or confirmation process

			// Verify that the grade has been deleted
			WebElement messageElement = Thread.findElement(By.id("Deletion complete"));
			assertThat(messageElement.getText()).isEqualTo("Grade deleted successfully");

		} catch (Exception ex) {
			throw ex;
		} finally {
			Thread.quit();
		}
	}

}
