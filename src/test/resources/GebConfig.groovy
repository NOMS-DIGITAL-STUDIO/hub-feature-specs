import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.chrome.ChromeDriver

driver = {
    new PhantomJSDriver(new DesiredCapabilities())

    //  Use the following to run tests with Chromedriver
    //  System.setProperty('webdriver.chrome.driver', 'path/to/chromedriver')
    //  new ChromeDriver()
}

