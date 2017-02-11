package com.airent.service.provider.connection;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebDriver implements AutoCloseable {

    private volatile org.openqa.selenium.WebDriver webDriver;
    private ProxyServer proxyServer;

    @Autowired
    public WebDriver(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    public org.openqa.selenium.WebDriver get() {
        initDriver();
        return webDriver;
    }

    private org.openqa.selenium.WebDriver initDriver() {
        if (null == webDriver) {
            synchronized (WebDriver.class) {
                if (null == webDriver) {
                    this.webDriver = createPhantomJs();
                    return webDriver;
                }
            }
        }
        return webDriver;
    }

    private org.openqa.selenium.WebDriver createPhantomJs() {
        PhantomJsDriverManager.getInstance().setup("2.1.1");

        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
        capabilities.setCapability("phantomjs.page.settings.userAgent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/53 (KHTML, like Gecko) Chrome/15.0.87");
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                new String[]{
                        //"--debug=true",
                        "--load-images=no",
                        "--proxy=" + proxyServer.getAddress(),
                        "--proxy-type=http",
                        "--proxy-auth=" + proxyServer.getAuthentication()
                });

        PhantomJSDriver driver = new PhantomJSDriver(capabilities);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.executePhantomJS("this.onResourceRequested = function(requestData, networkRequest) {\n" +
                "  var match = requestData.url.match(/^http[s]*:\\/\\/[www]*[/.]*avito/g);\n" +
                "  if (match == null) {\n" +
                "    networkRequest.cancel(); \n" +
                "  }" +
                "};");
        return driver;
    }

    private org.openqa.selenium.WebDriver createChrome() {
        ChromeDriverManager.getInstance().setup();

        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.addArguments("--proxy-server=socks5://" + proxyServer.getAddress()
//                + " --proxy-user-and-password=" + proxyServer.getAuthentication());
        chromeOptions.addArguments("--headless");

        ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);
        return chromeDriver;
    }

    private org.openqa.selenium.WebDriver createFirefox() {
        FirefoxDriverManager.getInstance().setup();

        FirefoxDriver firefoxDriver = new FirefoxDriver(DesiredCapabilities.firefox());
        return firefoxDriver;
    }

    @Override
    public void close() throws Exception {
        if (webDriver != null) {
            webDriver.close();
        }
    }

}