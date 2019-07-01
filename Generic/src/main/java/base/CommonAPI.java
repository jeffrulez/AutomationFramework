package base;


import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.LogStatus;
import org.apache.bcel.generic.Select;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import reporting.ExtentManager;
import reporting.ExtentTestManager;
import reporting.TestLogger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommonAPI {
    //ExtentReport
    public static ExtentReports extent;
    @BeforeSuite
    public void extentSetup(ITestContext context) {
        ExtentManager.setOutputDirectory(context);
        extent = ExtentManager.getInstance();
    }
    @BeforeMethod
    public void startExtent(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName().toLowerCase();
        ExtentTestManager.startTest(method.getName());
        ExtentTestManager.getTest().assignCategory(className);
    }
    protected String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
    @AfterMethod
    public void afterEachTestMethod(ITestResult result) {
        ExtentTestManager.getTest().getTest().setStartedTime(getTime(result.getStartMillis()));
        ExtentTestManager.getTest().getTest().setEndedTime(getTime(result.getEndMillis()));

        for (String group : result.getMethod().getGroups()) {
            ExtentTestManager.getTest().assignCategory(group);
        }

        if (result.getStatus() == 1) {
            ExtentTestManager.getTest().log(LogStatus.PASS, "Test Passed");
        } else if (result.getStatus() == 2) {
            ExtentTestManager.getTest().log(LogStatus.FAIL, getStackTrace(result.getThrowable()));
        } else if (result.getStatus() == 3) {
            ExtentTestManager.getTest().log(LogStatus.SKIP, "Test Skipped");
        }
        ExtentTestManager.endTest();
        extent.flush();
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(webDriver, result.getName());
        }
        webDriver.quit();
    }
    @AfterSuite
    public void generateReport() {
        extent.close();
    }
    private Date getTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

    public WebDriver webDriver = null;
    public String browserstack_username= "luillo1";
    public String browserstack_accesskey = "jmicniesmM3PUKea9trv";
    public String saucelabs_username = "";
    public String saucelabs_accesskey = "";

    @Parameters({"useCloudEnv","cloudEnvName","os","os_version","browserName","browserVersion","url"})
    @BeforeMethod
    public void setUp(@Optional("false") boolean useCloudEnv, @Optional("false")String cloudEnvName,
                      @Optional("windows") String os,@Optional("10") String os_version, @Optional("chrome-options") String browserName, @Optional("34")
                              String browserVersion, @Optional("automationpractice.com") String url)throws IOException {
        System.setProperty("webdriver.chrome.driver", "../Generic/driver/chromedriver.exe");
        if(useCloudEnv==true){
            if(cloudEnvName.equalsIgnoreCase("browserstack")) {
                getCloudDriver(cloudEnvName,browserstack_username,browserstack_accesskey,os,os_version, browserName, browserVersion);
            }else if (cloudEnvName.equalsIgnoreCase("saucelabs")){
                getCloudDriver(cloudEnvName,saucelabs_username, saucelabs_accesskey,os,os_version, browserName, browserVersion);
            }
        }else{

        }

        System.setProperty("webdriver.chrome.driver", "../Generic/drivers/chromedriver.exe");
        webDriver = new ChromeDriver();
        webDriver.manage().timeouts().pageLoadTimeout(25, TimeUnit.SECONDS);
        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        webDriver.get("http://automationpractice.com/index.php?controller=order");
        webDriver.manage().window().maximize();
    }
    public WebDriver getLocalDriver(@Optional("mac") String OS, String browserName){
        if(browserName.equalsIgnoreCase("chrome")){
            if(OS.equalsIgnoreCase("OS X")){
                System.setProperty("webdriver.chrome.driver", "../Generic/drivers/chromedriver");
            }else if(OS.equalsIgnoreCase("Windows")){
                System.setProperty("webdriver.chrome.driver", "../Generic/drivers/chromedriver.exe");
            }
            webDriver = new ChromeDriver();
        } else if(browserName.equalsIgnoreCase("chrome-options")){
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-notifications");
            if(OS.equalsIgnoreCase("OS X")){
                System.setProperty("webdriver.chrome.driver", "../Generic/driver/chromedriver");
            }else if(OS.equalsIgnoreCase("Windows")){
                System.setProperty("webdriver.chrome.driver", "../Generic/driver/chromedriver.exe");
            }
            webDriver = new ChromeDriver(options);
        }

        else if(browserName.equalsIgnoreCase("firefox")){
            if(OS.equalsIgnoreCase("OS X")){
                System.setProperty("webdriver.gecko.driver", "../Generic/browser-driver/geckodriver");
            }else if(OS.equalsIgnoreCase("Windows")) {
                System.setProperty("webdriver.gecko.driver", "../Generic/browser-driver/geckodriver.exe");
            }
            webDriver = new FirefoxDriver();

        } else if(browserName.equalsIgnoreCase("ie")) {
            System.setProperty("webdriver.ie.driver", "../Generic/browser-driver/IEDriverServer.exe");
            webDriver = new InternetExplorerDriver();
        }
        return webDriver;

    }


    public WebDriver getCloudDriver(String envName,String envUsername, String envAccessKey,String os, String os_version,String browserName,
                                    String browserVersion)throws IOException {
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setCapability("browser",browserName);
        cap.setCapability("browser_version",browserVersion);
        cap.setCapability("os", os);
        cap.setCapability("os_version", os_version);
        if(envName.equalsIgnoreCase("Saucelabs")){
            //resolution for Saucelabs
            webDriver = new RemoteWebDriver(new URL("http://"+envUsername+":"+envAccessKey+
                    "@ondemand.saucelabs.com:80/wd/hub"), cap);
        }else if(envName.equalsIgnoreCase("Browserstack")) {
            cap.setCapability("resolution", "1024x768");
            webDriver = new RemoteWebDriver(new URL("http://" + envUsername + ":" + envAccessKey +
                    "@hub-cloud.browserstack.com/wd/hub"), cap);
        }
        return webDriver;
    }

    @AfterMethod
    public void cleanUp(){
        webDriver.quit();
    }

    public void clickOnCss(String locator){
        webDriver.findElement(By.cssSelector(locator)).click();
    }
    public void clickOnElement(String locator){
        try {
            webDriver.findElement(By.cssSelector(locator)).click();
        }catch(Exception ex1){
            try {
                webDriver.findElement(By.xpath(locator)).click();
            }catch(Exception ex2){
                webDriver.findElement(By.id(locator)).click();
            }
        }
    }
    public void typeOnCss(String locator, String value){
        webDriver.findElement(By.cssSelector(locator)).sendKeys(value);
    }
    public void typeOnInputField(String locator, String value){
        try {
            webDriver.findElement(By.cssSelector(locator)).sendKeys(value);
        }catch (Exception ex){
            webDriver.findElement(By.id(locator)).sendKeys(value);
        }

    }
    public void clickByXpath(String locator) {
        webDriver.findElement(By.xpath(locator)).click();
    }

    public void typeByCss(String locator, String value) {
        webDriver.findElement(By.cssSelector(locator)).sendKeys(value);
    }
    public void typeByCssNEnter(String locator, String value) {
        webDriver.findElement(By.cssSelector(locator)).sendKeys(value, Keys.ENTER);
    }

    public void typeByXpath(String locator, String value) {
        webDriver.findElement(By.xpath(locator)).sendKeys(value);
    }

    public void takeEnterKeys(String locator) {
        webDriver.findElement(By.cssSelector(locator)).sendKeys(Keys.ENTER);
    }

    public void clearInputField(String locator){
        webDriver.findElement(By.cssSelector(locator)).clear();
    }
    public List<WebElement> getListOfWebElementsById(String locator) {
        List<WebElement> list = new ArrayList<WebElement>();
        list = webDriver.findElements(By.id(locator));
        return list;
    }
    public List<String> getTextFromWebElements(String locator){
        List<WebElement> element = new ArrayList<WebElement>();
        List<String> text = new ArrayList<String>();
        element = webDriver.findElements(By.cssSelector(locator));
        for(WebElement web:element){
            String st = web.getText();
            text.add(st);
        }

        return text;
    }
    public List<WebElement> getListOfWebElementsByCss(String locator) {
        List<WebElement> list = new ArrayList<WebElement>();
        list = webDriver.findElements(By.cssSelector(locator));
        return list;
    }
    public List<WebElement> getListOfWebElementsByXpath(String locator) {
        List<WebElement> list = new ArrayList<WebElement>();
        list = webDriver.findElements(By.xpath(locator));
        return list;
    }
    public String  getCurrentPageUrl(){
        String url = webDriver.getCurrentUrl();
        return url;
    }
    public void navigateBack(){
        webDriver.navigate().back();
    }
    public void navigateForward(){
        webDriver.navigate().forward();
    }
    public String getTextByCss(String locator){
        String st = webDriver.findElement(By.cssSelector(locator)).getText();
        return st;
    }
    public String getTextByXpath(String locator){
        String st = webDriver.findElement(By.xpath(locator)).getText();
        return st;
    }
    public String getTextById(String locator){
        return webDriver.findElement(By.id(locator)).getText();
    }
    public String getTextByName(String locator){
        String st = webDriver.findElement(By.name(locator)).getText();
        return st;
    }

    public List<String> getListOfString(List<WebElement> list) {
        List<String> items = new ArrayList<String>();
        for (WebElement element : list) {
            items.add(element.getText());
        }
        return items;
    }


    public static void sleepFor(int sec)throws InterruptedException{
        Thread.sleep(sec * 1000);
    }
    public void mouseHoverByCSS(String locator){
        try {
            WebElement element = webDriver.findElement(By.cssSelector(locator));
            Actions action = new Actions(webDriver);
            Actions hover = action.moveToElement(element);
        }catch(Exception ex){
            System.out.println("First attempt has been done, This is second try");
            WebElement element = webDriver.findElement(By.cssSelector(locator));
            Actions action = new Actions(webDriver);
            action.moveToElement(element).perform();

        }

    }
    public void mouseHoverByXpath(String locator){
        try {
            WebElement element = webDriver.findElement(By.xpath(locator));
            Actions action = new Actions(webDriver);
            Actions hover = action.moveToElement(element);
        }catch(Exception ex){
            System.out.println("First attempt has been done, This is second try");
            WebElement element = webDriver.findElement(By.cssSelector(locator));
            Actions action = new Actions(webDriver);
            action.moveToElement(element).perform();

        }

    }
    //handling Alert
    public void okAlert(){
        Alert alert = webDriver.switchTo().alert();
        alert.accept();
    }
    public void cancelAlert(){
        Alert alert = webDriver.switchTo().alert();
        alert.dismiss();
    }

    //iFrame Handle
    public void iframeHandle(WebElement element){
        webDriver.switchTo().frame(element);
    }

    public void goBackToHomeWindow(){
        webDriver.switchTo().defaultContent();
    }

    //get Links
    public void getLinks(String locator){
        webDriver.findElement(By.linkText(locator)).findElement(By.tagName("a")).getText();
    }

    public static void captureScreenshot(WebDriver driver, String screenshotName){
        DateFormat df = new SimpleDateFormat("(MM.dd.yyyy-HH:mma)");
        Date date = new Date();
        df.format(date);

        File file = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(file, new File(System.getProperty("user.dir")+ "/screenshots/"+screenshotName+".png"));
            System.out.println("Screenshot captured");
        } catch (Exception e) {
            System.out.println("Exception while taking screenshot "+e.getMessage());;
        }

    }
    //Taking Screen shots
    public void takeScreenShot()throws IOException {
        File file = ((TakesScreenshot)webDriver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(file,new File("screenShots.png"));
    }
    //Synchronization
    public void waitUntilClickAble(By locator){
        WebDriverWait wait = new WebDriverWait(webDriver, 50);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    public void waitUntilVisible(By locator){
        WebDriverWait wait = new WebDriverWait(webDriver, 10);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    public void waitUntilSelectable(By locator){
        WebDriverWait wait = new WebDriverWait(webDriver, 10);
        boolean element = wait.until(ExpectedConditions.elementToBeSelected(locator));
    }
    public void upLoadFile(String locator,String path){
        webDriver.findElement(By.cssSelector(locator)).sendKeys(path);
        /* path example to upload a file/image
           path= "C:\\Users\\rrt\\Pictures\\ds1.png";
         */
    }
    public void clearInput(String locator){
        webDriver.findElement(By.cssSelector(locator)).clear();
    }
    public void keysInput(String locator){
        webDriver.findElement(By.cssSelector(locator)).sendKeys(Keys.ENTER);
    }
    public static String convertToString(String st){
        String splitString ;
        splitString = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(st), ' ');
        return splitString;
    }
    //Handling New Tabs
    public static WebDriver handleNewTab(WebDriver driver1){
        String oldTab = driver1.getWindowHandle();
        List<String> newTabs = new ArrayList<String>(driver1.getWindowHandles());
        newTabs.remove(oldTab);
        driver1.switchTo().window(newTabs.get(0));
        return driver1;
    }
    public static boolean isPopUpWindowDisplayed(WebDriver driver1, String locator){
        boolean value = driver1.findElement(By.cssSelector(locator)).isDisplayed();
        return value;
    }

    public void typeOnInputBox(String locator, String value) {
        try{
            webDriver.findElement(By.id(locator)).sendKeys(value, Keys.ENTER);
        }catch (Exception ex1) {
            System.out.println("ID locator didn't work");
        }
        try{
            webDriver.findElement(By.name(locator)).sendKeys(value, Keys.ENTER);
        }catch (Exception ex2){
            System.out.println("Name locator didn't work");
        }
        try{
            webDriver.findElement(By.cssSelector(locator)).sendKeys(value, Keys.ENTER);
        }catch (Exception ex3){
            System.out.println("CSS locator didn't work");
        }
    }
    public void SendKeys(WebElement element, String keys, String elementName){
        TestLogger.log("Sending Keys to " + elementName);
        element.sendKeys(keys);
        TestLogger.log("Keys Sent Successfully to "+ elementName);
    }
    public void inputValueInTextBoxByWebElement(WebElement webElement, String value) {
        webElement.sendKeys(value + Keys.ENTER);
    }
    public void clearInputBox(WebElement webElement){
        webElement.clear();
    }
    public String getTextByWebElement(WebElement webElement) {
        String text = webElement.getText();
        return text;
    }
    public void clearInputByXpath(String locator){
        webDriver.findElement(By.xpath(locator)).clear();
    }
    public void typeByXpathNEnter(String locator, String value){
        webDriver.findElement(By.cssSelector(locator)).sendKeys(value);
    }

}
