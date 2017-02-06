package com.airent.service.provider.avito;

import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.ParsedAdvert;
import com.airent.service.provider.api.ParsedAdvertHeader;
import com.airent.service.provider.proxy.ProxyServer;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.airent.service.provider.common.Util.getNumberInsideOf;

@Service
public class AvitoAdvertsProvider implements AdvertsProvider, AutoCloseable {

    private Logger logger = LoggerFactory.getLogger(AvitoAdvertsProvider.class);

    private static final String MAIN_PAGE_URL = "https://www.avito.ru/kazan/kvartiry/sdam/na_dlitelnyy_srok";
    private static final String PAGE_INDEX_SUFFIX = "?p=";
    private static final int MAX_PAGES = 50;

    private Pattern imageUrlPattern = Pattern.compile(".*background-image:[ ]*url[ ]*\\(.*//([a-zA-Z0-9/.]*)[\"]*\\).*");

    private volatile WebDriver driver;
    private ProxyServer proxyServer;
    private AvitoDateFormatter avitoDateFormatter;
    private AvitoPhoneParser avitoPhoneParser;
    private int maxItemsToScan;

    public AvitoAdvertsProvider(ProxyServer proxyServer,
                                AvitoDateFormatter avitoDateFormatter,
                                AvitoPhoneParser avitoPhoneParser,
                                @Value("${avito.provider.max.items}") int maxItemsToScan) {
        this.proxyServer = proxyServer;
        this.avitoDateFormatter = avitoDateFormatter;
        this.avitoPhoneParser = avitoPhoneParser;
        this.maxItemsToScan = maxItemsToScan;
    }

    public WebDriver initDriver() {
        if (null == driver) {
            synchronized (WebDriver.class) {
                if (null == driver) {
                    PhantomJsDriverManager.getInstance().setup("2.1.1");

                    DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
                    capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                            new String[]{"--load-images=no",
                                    "--proxy=" + proxyServer.getAddress(),
                                    "--proxy-type=socks5",
                                    "--proxy-auth=" + proxyServer.getAuthentication()});

                    PhantomJSDriver driver = new PhantomJSDriver(capabilities);
                    driver.executePhantomJS("this.onResourceRequested = function(requestData, networkRequest) {\n" +
                            "  var match = requestData.url.match(/^http[s]*:\\/\\/[www]*[/.]*avito/g);\n" +
                            "  if (match == null) {\n" +
                            "    networkRequest.cancel(); \n" +
                            "  }" +
                            "};");

                    logger.info("Browser original window size is {}", driver.manage().window().getSize());
                    this.driver = driver;
                    return driver;
                }
            }
        }
        return driver;
    }

    @Override
    public void close() throws Exception {
        initDriver();
        driver.close();
    }

    @Override
    public String getType() {
        return "AVT";
    }


    @Override
    public int getMaxItemsToScan() {
        return maxItemsToScan;
    }

    @Override
    public Iterator<ParsedAdvertHeader> getHeaders() {
        initDriver();
        // open adverts page and remember position on page
        return new Iterator<ParsedAdvertHeader>() {

            private int pageNumber = 0;
            private Iterator<ParsedAdvertHeader> currentPageHeaders;

            @Override
            public boolean hasNext() {
                return pageNumber < MAX_PAGES;
            }

            @Override
            public ParsedAdvertHeader next() {
                if (!hasNext()) {
                    throw new IllegalStateException("There is not next for you, dummy");
                }

                if (currentPageHeaders == null || !currentPageHeaders.hasNext()) {
                    long startTime = System.currentTimeMillis();

                    // open next page
                    if (pageNumber == 0) {
                        driver.get(MAIN_PAGE_URL);
                    } else {
                        driver.get(MAIN_PAGE_URL + PAGE_INDEX_SUFFIX + pageNumber);
                    }
                    currentPageHeaders = driver
                            .findElements(By.className("item")).stream()
                            .map(header -> {
                                ParsedAdvertHeader parsedAdvertHeader = new ParsedAdvertHeader();
                                parsedAdvertHeader.setAdvertUrl(
                                        header.findElement(By.className("item-description-title-link")).getAttribute("href"));
                                parsedAdvertHeader.setPublicationTimestamp(avitoDateFormatter.getTimestamp(
                                        header.findElement(By.className("date")).getAttribute("innerText")));
                                return parsedAdvertHeader;
                            }).collect(Collectors.toList()).iterator();

                    logger.info("Spend time for headers page opening {} : {} ms", pageNumber, System.currentTimeMillis() - startTime);

                    pageNumber++;
                }

                if (!currentPageHeaders.hasNext()) {
                    String bodyText = driver.findElement(By.tagName("html")).getAttribute("innerHTML");
                    logger.error("Iterator has no next element. Page is {}", bodyText);
                }

                return currentPageHeaders.next();
            }
        };
    }

    @Override
    public ParsedAdvert getAdvert(ParsedAdvertHeader parsedAdvertHeader) {
        initDriver();

        openPageAndPhone(parsedAdvertHeader.getAdvertUrl());

        ParsedAdvert parsedAdvert = new ParsedAdvert();

        parsedAdvert.setPublicationTimestamp(parsedAdvertHeader.getPublicationTimestamp());
        parsedAdvert.setBedrooms(1);
        parsedAdvert.setBeds(1);
        parsedAdvert.setAddress(getAddress());
        parsedAdvert.setRooms(getRooms());
        parsedAdvert.setFloor(getFloor());
        parsedAdvert.setMaxFloor(getMaxFloor());
        parsedAdvert.setSq(getSq());
        parsedAdvert.setDescription(getDescription());
        parsedAdvert.setLatitude(getCoordinates().getLeft());
        parsedAdvert.setLongitude(getCoordinates().getRight());
        parsedAdvert.setPrice(getPrice());

        parsedAdvert.setUserName(getUserName());
        parsedAdvert.setPhone(getPhone());
        parsedAdvert.setTrustRate(getTrustRate());

        parsedAdvert.setPhotos(getPhotos());

        return parsedAdvert;
    }

    private void openPageAndPhone(String advertUrl) {
        long startTime = System.currentTimeMillis();

        driver.get(advertUrl);

        logger.info("Spend time for opening advert {} : {} ms", advertUrl, System.currentTimeMillis() - startTime);


        long phoneStartTime = System.currentTimeMillis();

        // click on phone button
        WebElement phoneButton = driver.findElement(By.className("item-phone-number"))
                .findElement(By.tagName("button"));
        phoneButton.click();

        // click by js
        //((JavascriptExecutor)driver).executeScript("$('.js-item-phone-button').click()");

        try {
            new WebDriverWait(driver, 50)
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector(".item-phone-big-number img")));
        } catch (TimeoutException e) {
            logger.error("Failed to find element on page", e);
        }

        logger.info("Spend time for phone opening of advert {} : {} ms", advertUrl, System.currentTimeMillis() - phoneStartTime);
    }

    private String getAddress() {
        return driver
                .findElement(By.className("item-view-main"))
                .findElement(By.cssSelector(".item-map-address [itemprop=streetAddress]"))
                .getAttribute("innerText")
                .trim();
    }

    private long getPhone() {
        String phoneVal = driver
                .findElement(By.cssSelector(".item-phone-big-number img"))
                .getAttribute("src");
        return avitoPhoneParser.parseNumbersFromImage(phoneVal);
    }

    private Integer getPrice() {
        return getNumberInsideOf(driver
                .findElement(By.className("item-price"))
                .findElement(By.className("price-value-string"))
                .getAttribute("innerText"));
    }

    private Integer getRooms() {
        List<WebElement> itemParams = driver
                .findElement(By.className("item-view-main"))
                .findElement(By.className("item-params"))
                .findElements(By.className("item-params-list-item"));
        return getNumberInsideOf(itemParams.get(0).getAttribute("innerText"));
    }

    private Integer getFloor() {
        List<WebElement> itemParams = driver
                .findElement(By.className("item-view-main"))
                .findElement(By.className("item-params"))
                .findElements(By.className("item-params-list-item"));
        return getNumberInsideOf(itemParams.get(1).getAttribute("innerText"));
    }

    private Integer getMaxFloor() {
        List<WebElement> itemParams = driver
                .findElement(By.className("item-view-main"))
                .findElement(By.className("item-params"))
                .findElements(By.className("item-params-list-item"));
        return getNumberInsideOf(itemParams.get(2).getAttribute("innerText"));
    }

    private Integer getSq() {
        List<WebElement> itemParams = driver
                .findElement(By.className("item-view-main"))
                .findElement(By.className("item-params"))
                .findElements(By.className("item-params-list-item"));
        return getNumberInsideOf(itemParams.get(4).getAttribute("innerText"));
    }

    private String getDescription() {
        return driver
                .findElement(By.className("item-view-main"))
                .findElement(By.cssSelector(".item-description-text p")).getAttribute("innerText");
    }

    private Pair<Double, Double> getCoordinates() {
        WebElement searchMap = driver.findElement(By.className("b-search-map"));
        String latVal = searchMap.getAttribute("data-map-lat");
        String lonVal = searchMap.getAttribute("data-map-lon");

        double latitude = Double.parseDouble(latVal);
        double longitude = Double.parseDouble(lonVal);
        return Pair.of(latitude, longitude);
    }

    private String getUserName() {
        WebElement contacts = driver.findElement(By.className("item-view-contacts"));
        return contacts.findElement(By.className("seller-info-name")).getAttribute("innerText").trim();
    }

    private int getTrustRate() {
        WebElement sellerInfoLabel = driver
                .findElement(By.className("seller-info"))
                .findElement(By.className("seller-info-label"));
        if ("агентство".equals(sellerInfoLabel.getAttribute("innerText").trim().toLowerCase())) {
            return 1;
        }
        return 5000;
    }

    private List<String> getPhotos() {
        try {
            List<WebElement> photos = driver.findElement(By.className("item-view-gallery"))
                    .findElements(By.className("gallery-list-item-link"));

            return photos.stream()
                    .map(photo -> getImageUrl(photo.getAttribute("style")))
                    .map(photo -> photo.replace("80x60", "640x480"))
                    .map(v -> "http://" + v)
                    .collect(Collectors.toList());
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    String getImageUrl(String fullImageUrl) {
        Matcher matcher = imageUrlPattern.matcher(fullImageUrl);
        if (!matcher.matches()) {
            throw new IllegalStateException("Failed to retrieve image from " + fullImageUrl);
        }
        return matcher.group(1);
    }

}