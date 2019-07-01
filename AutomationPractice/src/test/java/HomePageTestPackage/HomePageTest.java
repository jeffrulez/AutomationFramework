package HomePageTestPackage;

import HomePagePackage.HomePage;
import base.CommonAPI;
import org.openqa.selenium.support.PageFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reporting.TestLogger;

public class HomePageTest extends HomePage {

    HomePage homePage;
    @BeforeMethod
    public void init(){
        homePage = PageFactory.initElements(webDriver, HomePage.class);
    }

    @Test
    public void searchBoxTest(){
        TestLogger.log(getClass().getSimpleName() + ": " + CommonAPI.convertToString(new Object() {
        }.getClass().getEnclosingMethod().getName()));
        sendKeysSearch();
    }
}
