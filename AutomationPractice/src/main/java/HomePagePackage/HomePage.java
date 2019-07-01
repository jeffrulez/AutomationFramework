package HomePagePackage;

import base.CommonAPI;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import reporting.TestLogger;


public class HomePage extends CommonAPI {

    @FindBy(xpath = "//input[@id='search_query_top']")public static WebElement searchBox;
    @FindBy(xpath = "//button[@name='submit_search']")public static WebElement clickSearch;
    @FindBy(xpath = "//p[@class='alert alert-warning']")public static WebElement waring;


    public void sendKeysSearch(){
        searchBox.sendKeys("jeff");
        waitUntilClickAble(By.xpath("//button[@name='submit_search']"));
        clickSearch.click();
        String ActualValue = waring.getText();
        String ExpectedValue = "No results were found for your search \"jeff\"";
        Assert.assertEquals(ActualValue,ExpectedValue);
        TestLogger.log("klk con klk");
    }
}
