package com.airent.service.provider.avito;

import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.ParsedAdvert;
import com.airent.service.provider.api.ParsedAdvertHeader;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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

    private static final String MAIN_PAGE_URL = "https://www.avito.ru/kazan/kvartiry/sdam/na_dlitelnyy_srok";
    private static final String PAGE_INDEX_SUFFIX = "?p=";
    private Pattern imageUrlPattern = Pattern.compile(".*background-image:[ ]*url[ ]*\\(//(.*)\\).*");

    private WebDriver driver;

    private AvitoDateFormatter avitoDateFormatter;
    private AvitoPhoneParser avitoPhoneParser;
    private int maxPages;

    public AvitoAdvertsProvider(AvitoDateFormatter avitoDateFormatter,
                                AvitoPhoneParser avitoPhoneParser,
                                @Value("${avito.provider.max.items}") int maxPages) {
        this.avitoDateFormatter = avitoDateFormatter;
        this.avitoPhoneParser = avitoPhoneParser;
        this.maxPages = maxPages;

        ChromeDriverManager.getInstance().setup();
        this.driver = new ChromeDriver();
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    @Override
    public String getType() {
        return "AVT";
    }

    @Override
    public Iterator<ParsedAdvertHeader> getHeaders() {
        // open adverts page and remember position on page
        return new Iterator<ParsedAdvertHeader>() {

            private int pageNumber = 0;
            private Iterator<ParsedAdvertHeader> currentPageHeaders;

            @Override
            public boolean hasNext() {
                return pageNumber < maxPages || (currentPageHeaders != null && currentPageHeaders.hasNext());
            }

            @Override
            public ParsedAdvertHeader next() {
                if (currentPageHeaders == null || !currentPageHeaders.hasNext()) {
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
                                        header.findElement(By.className("date")).getText()));
                                return parsedAdvertHeader;
                            }).collect(Collectors.toList()).iterator();
                    pageNumber++;
                }

                return currentPageHeaders.next();
            }
        };
    }

    @Override
    public ParsedAdvert getAdvert(ParsedAdvertHeader parsedAdvertHeader) {
        openPageAndPhone(parsedAdvertHeader.getAdvertUrl());

        ParsedAdvert parsedAdvert = new ParsedAdvert();

        parsedAdvert.setPublicationTimestamp(parsedAdvertHeader.getPublicationTimestamp());
        parsedAdvert.setBedrooms(1);
        parsedAdvert.setAddress(getAddress());
        parsedAdvert.setRooms(getRooms());
        parsedAdvert.setFloor(getFloor());
        parsedAdvert.setSq(getSq());
        parsedAdvert.setDescription(getDescription());
        parsedAdvert.setLatitude(getCoordinates().getLeft());
        parsedAdvert.setLongitude(getCoordinates().getRight());

        parsedAdvert.setUserName(getUserName());
        parsedAdvert.setPhone(getPhone());
        parsedAdvert.setTrustRate(getTrustRate());

        parsedAdvert.setPhotos(getPhotos());

        return parsedAdvert;
    }

    private void openPageAndPhone(String advertUrl) {
        driver.get(advertUrl);

        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.className("item-phone-number")))
                .click();

        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".item-phone-big-number img")));

    }

    private String getAddress() {
        return driver
                .findElement(By.className("item-view-main"))
                .findElement(By.cssSelector(".item-map-address [itemprop=streetAddress]"))
                .getText();
    }

    private long getPhone() {
        String phoneVal = driver
                .findElement(By.cssSelector(".item-phone-big-number img"))
                .getAttribute("src");
        return avitoPhoneParser.parseNumbersFromImage(phoneVal);
    }

    private Integer getRooms() {
        List<WebElement> itemParams = driver
                .findElement(By.className("item-view-main"))
                .findElement(By.className("item-params"))
                .findElements(By.className("item-params-list-item"));
        return getNumberInsideOf(itemParams.get(0).getText());
    }

    private Integer getFloor() {
        List<WebElement> itemParams = driver
                .findElement(By.className("item-view-main"))
                .findElement(By.className("item-params"))
                .findElements(By.className("item-params-list-item"));
        return getNumberInsideOf(itemParams.get(1).getText());
    }

    private Integer getSq() {
        List<WebElement> itemParams = driver
                .findElement(By.className("item-view-main"))
                .findElement(By.className("item-params"))
                .findElements(By.className("item-params-list-item"));
        return getNumberInsideOf(itemParams.get(3).getText());
    }

    private String getDescription() {
        return driver
                .findElement(By.className("item-view-main"))
                .findElement(By.cssSelector(".item-description-text p")).getText();
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
        return contacts.findElement(By.className("seller-info-name")).getText().trim();
    }

    private int getTrustRate() {
        WebElement sellerInfoLabel = driver
                .findElement(By.className("seller-info"))
                .findElement(By.className("seller-info-label"));
        if ("агентство".equals(sellerInfoLabel.getText().trim().toLowerCase())) {
            return 1;
        }
        return 5000;
    }

    private List<String> getPhotos() {
        List<WebElement> photos = driver.findElement(By.className("item-view-gallery"))
                .findElements(By.className("gallery-list-item-link"));

        return photos.stream()
                .map(photo -> getImageUrl(photo.getAttribute("style")))
                .map(photo -> photo.replace("80x60", "640x480"))
                .collect(Collectors.toList());
    }

    private String getImageUrl(String fullImageUrl) {
        Matcher matcher = imageUrlPattern.matcher(fullImageUrl);
        if (!matcher.matches()) {
            throw new IllegalStateException("Failed to retrieve image from " + fullImageUrl);
        }
        return matcher.group(1);
    }

}