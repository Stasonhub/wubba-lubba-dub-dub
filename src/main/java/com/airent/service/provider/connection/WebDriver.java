package com.airent.service.provider.connection;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.internal.Base64Encoder;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
                    this.webDriver = createChrome();
                    return webDriver;
                }
            }
        }
        return webDriver;
    }

    private org.openqa.selenium.WebDriver createChrome() {
        try {
            ChromeDriverManager.getInstance().setup();

            ClassPathResource classPathResource = new ClassPathResource("chrome/extensions/Block-image_v1.1.crx");
            String encoded = new Base64Encoder().encode(IOUtils.toByteArray(classPathResource.getInputStream()));

            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addEncodedExtensions(encoded);
            chromeOptions.addArguments("--start-maximized");
            chromeOptions.addArguments("--disable-web-security");
            chromeOptions.addArguments("--allow-running-insecure-content");
            //chromeOptions.addArguments("--headless"); is not working yet
            chromeOptions.addArguments("--no-sandbox");

            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability(CapabilityType.PROXY, proxyServer.getSeleniumProxy());
            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

            return new ChromeDriver(capabilities);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (webDriver != null) {
            webDriver.close();
        }
    }

}