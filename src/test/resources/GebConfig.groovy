import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.DesiredCapabilities

driver = {
    new PhantomJSDriver(new DesiredCapabilities())

    // Use the following to run tests with Chromedriver
    // example path for OSX would be: '/usr/local/bin/chromedriver'
    //  Hub.setProperty('webdriver.chrome.driver', 'path/to/chromedriver')
    //  new ChromeDriver()
}

