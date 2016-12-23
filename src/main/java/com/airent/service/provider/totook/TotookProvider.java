package com.airent.service.provider.totook;

import com.airent.config.MvcConfig;
import com.airent.model.Advert;
import com.airent.model.Photo;
import com.airent.model.User;
import com.airent.service.LocationService;
import com.airent.service.PhotoService;
import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.RawAdvert;
import com.airent.service.provider.common.Util;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.airent.service.provider.common.Util.*;

@Component
public class TotookProvider implements AdvertsProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int MAX_PAGES = 20;
    private static final String MAIN_PAGE_URL = "http://kazan.totook.ru/catalog/?PARENT_SECTION=28";
    private static final String USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36";
    private static final String AUTH_COOKIE = "PHPSESSID";

    private Pattern headerPattern = Pattern.compile(".*([0-9])-комн.*([0-9]+) м");
    private Pattern streetPattern = Pattern.compile(".,.*,(.*,.*)");
    private Pattern floorPattern = Pattern.compile(".*([0-9])+ из ([0-9])+.*");
    private Pattern coordinatesPattern = Pattern.compile(".*coordinates: \\[([0-9]+.[0-9]+), ([0-9]+.[0-9]+)\\].*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    private Pattern phoneNumberPattern = Pattern.compile(".*>\\+([0-9- ]+).*");
    private Pattern imageSrcPattern = Pattern.compile("(.*)&w=[0-9]+&h=[0-9]+");

    private LocationService locationService;
    private PhotoService photoService;
    private String storagePath;
    private int maxItems;
    private TotookDateFormatter totookDateFormatter;

    @Autowired
    public TotookProvider(LocationService locationService,
                          PhotoService photoService,
                          TotookDateFormatter totookDateFormatter,
                          @Value("${totook.provider.max.items}") int maxItems,
                          @Value("${external.storage.path}") String storagePath) {
        this.locationService = locationService;
        this.photoService = photoService;
        this.totookDateFormatter = totookDateFormatter;
        this.maxItems = maxItems;
        this.storagePath = storagePath;
    }

    @Override
    public String getType() {
        return "TTK";
    }

    @Override
    public List<RawAdvert> getAdvertsUntil(long timestamp) {
        List<RawAdvert> result = new ArrayList<>();
        try {
            List<Pair<String, Long>> advertIdCollector = new ArrayList<>();

            c1:
            for (int i = 0; i < MAX_PAGES; i++) {
                Document doc = Jsoup
                        .connect(MAIN_PAGE_URL + (i != 0 ? "&PAGEN_2=" + (i + 1) : ""))
                        .userAgent(USER_AGENT)
                        .get();
                Elements itemsOnPage = doc.select(".b-catalog__object");
                for (Element item : itemsOnPage) {
                    long itemTimestamp = getTimestamp(item);
                    if (itemTimestamp <= (timestamp + 60_000) || advertIdCollector.size() >= maxItems) {
                        // we have reached previous scan point or limits
                        break c1;
                    }

                    advertIdCollector.add(Pair.of(getId(item), itemTimestamp));
                }
            }

            String authToken = login();
            for (Pair<String, Long> item : advertIdCollector) {
                RawAdvert advert = getAdvert(item.getLeft(), item.getRight(), authToken);
                if (advert != null) {
                    result.add(advert);
                }
            }
        } catch (IOException e) {
            logger.error("Failure in parsing", e);
        }
        return result;
    }

    private long getTimestamp(Element element) {
        return totookDateFormatter.getTimestamp(element.select(".b-catalog__object__date").text());
    }

    private String getId(Element element) {
        return element.select("a").attr("href");
    }

    private String login() throws IOException {
        Connection.Response response = Jsoup.connect("http://kazan.totook.ru/cabinet/?login=yes")
                .referrer("http://kazan.totook.ru/cabinet/")
                .method(Connection.Method.POST)
                .userAgent(USER_AGENT)
                .data("AUTH_FORM", "Y")
                .data("TYPE", "AUTH")
                .data("backurl", "/cabinet/")
                .data("USER_LOGIN", "kakakattk@mail.ru")
                .data("USER_PASSWORD", "qwerty12345")
                .data("USER_REMEMBER", "Y")
                .data("Login", "Войти на сайт")
                .execute();
        String authToken = response.cookies().get(AUTH_COOKIE);
        if (authToken == null) {
            throw new IllegalArgumentException("Authorization failure " + authToken);
        }
        return authToken;
    }

    private RawAdvert getAdvert(String itemId, long timestamp, String authToken) throws IOException {
        Document advertDocument = Jsoup.connect("http://kazan.totook.ru" + itemId)
                .userAgent(Util.USER_AGENT)
                .cookie(AUTH_COOKIE, authToken)
                .get();

        /* ------- advert ------- */
        Elements header = advertDocument.select(".main-inner h1");

        int roomsCount = getRoomsCountFromHeader(header.text());
        int sq = getSqFromHeader(header.text());
        int price = getNumberInsideOf(advertDocument.select(".b-detail__description__price__summ").text());
        String address = getAddress(advertDocument.select(".b-detail__address").text());

        Elements floorElements = advertDocument.select(".features_item.photos p");
        Pair<Integer, Integer> floorInfo = getFloorInfo(floorElements.iterator().next().text());
        int floor = floorInfo.getLeft();
        int maxFloor = floorInfo.getRight();

        String description = advertDocument.select(".b-detail__description__content p").text();

        Elements mapScript = advertDocument.select(".b-detail__map + script");
        Pair<Double, Double> coordinates = getCoordinates(mapScript.iterator().next().data());
        double latitude = coordinates.getLeft();
        double longitude = coordinates.getRight();

        Advert advert = new Advert();
        advert.setBedrooms(1);
        advert.setRooms(roomsCount);
        advert.setSq(sq);
        advert.setFloor(floor);
        advert.setMaxFloor(maxFloor);
        advert.setAddress(address);
        advert.setDescription(description);
        advert.setPublicationDate(timestamp);
        advert.setLatitude(latitude);
        advert.setLongitude(longitude);
        advert.setDistrict(locationService.getDistrictFromAddress(latitude, longitude));
        advert.setRaw(true);
        advert.setPrice(price);

        /* ------- user ------- */
        Connection.Response phoneResponse = Jsoup.connect("http://kazan.totook.ru/bitrix/templates/totook_adaptive/aj/phone.php")
                .referrer(":http://kazan.totook.ru" + itemId)
                .userAgent(Util.USER_AGENT)
                .data("tCan", "Y")
                .data("tID", String.valueOf(getNumberInsideOf(itemId)))
                .ignoreContentType(true)
                .cookie(AUTH_COOKIE, authToken)
                .method(Connection.Method.POST)
                .execute();

        long phoneNumber = getPhoneNumber(phoneResponse.body());

        User user = new User();
        user.setName("");
        user.setPhone(phoneNumber);
        user.setRegistered(false);
        user.setTrustRate(1_000);

        /* ------- photos ------- */
        List<Photo> photos = new ArrayList<>();
        Elements images = advertDocument.select(".b-detail__photos a img");

        if (images.isEmpty()) {
            logger.warn("Not found photos for advert " + itemId);
            return null;
        }

        long photosPathId = System.currentTimeMillis();

        int index = 0;
        for (Element image : images) {
            String imageUrl = getImageUrl(image.attr("src"));
            Connection.Response response =
                    Jsoup.connect("http://kazan.totook.ru" + imageUrl + "&w=1600")
                            .userAgent(Util.USER_AGENT)
                            .ignoreContentType(true)
                            .cookie(AUTH_COOKIE, authToken)
                            .execute();

            String path = storagePath + File.separator + "t" + File.separator + photosPathId + File.separator + index + ".jpg";
            new File(path).getParentFile().mkdirs();

            BufferedImage bufferedImage = removeAvitoSign(ImageIO.read(new ByteArrayInputStream(response.bodyAsBytes())));
            try (FileOutputStream out = new FileOutputStream(new java.io.File(path))) {
                ImageIO.write(bufferedImage, "jpeg", out);
            }

            Photo photo = new Photo();
            photo.setPath(MvcConfig.STORAGE_FILES_PREFIX + File.separator + "t" + File.separator + photosPathId + File.separator + index + ".jpg");
            photo.setMain(index == 0);
            photo.setHash(photoService.calculateHash(bufferedImage));
            photos.add(photo);

            index++;
        }

        RawAdvert rawAdvert = new RawAdvert();
        rawAdvert.setAdvert(advert);
        rawAdvert.setUser(user);
        rawAdvert.setPhotos(photos);
        return rawAdvert;
    }

    private int getRoomsCountFromHeader(String headerText) {
        Matcher headerMatcher = headerPattern.matcher(headerText);
        if (headerMatcher.find()) {
            return Integer.valueOf(headerMatcher.group(1));
        }
        throw new IllegalArgumentException("Failed to retrieve rooms count from " + headerText);
    }

    private int getSqFromHeader(String headerText) {
        Matcher headerMatcher = headerPattern.matcher(headerText);
        if (headerMatcher.find()) {
            return Integer.valueOf(headerMatcher.group(2));
        }
        throw new IllegalArgumentException("Failed to retrieve sq from " + headerText);
    }

    private String getAddress(String detailedAddressString) {
        Matcher detailedAddressMatcher = streetPattern.matcher(detailedAddressString);
        if (detailedAddressMatcher.find()) {
            return detailedAddressMatcher.group(1);
        }
        throw new IllegalArgumentException("Failed to get address from " + detailedAddressString);
    }

    private Pair<Integer, Integer> getFloorInfo(String floorInfoText) {
        Matcher floorMatcher = floorPattern.matcher(floorInfoText);
        if (floorMatcher.matches()) {
            return Pair.of(Integer.valueOf(floorMatcher.group(1)), Integer.valueOf(floorMatcher.group(2)));
        }
        throw new IllegalArgumentException("Failed to get floor info from " + floorInfoText);
    }

    Pair<Double, Double> getCoordinates(String coordinatesText) {
        Matcher coordinatesMatcher = coordinatesPattern.matcher(coordinatesText);
        if (coordinatesMatcher.matches()) {
            return Pair.of(Double.valueOf(coordinatesMatcher.group(2)), Double.valueOf(coordinatesMatcher.group(1)));
        }
        throw new IllegalArgumentException("Failed to get coordinates from " + coordinatesText);
    }

    private long getPhoneNumber(String phoneNumberText) {
        Matcher phoneNumberMatcher = phoneNumberPattern.matcher(phoneNumberText);
        if (phoneNumberMatcher.matches()) {
            return getLongNumberInsideOf(phoneNumberMatcher.group(1));
        }
        throw new IllegalArgumentException("Failed to get phone number from " + phoneNumberText);
    }

    String getImageUrl(String imageUrl) {
        Matcher imageSrcMatcher = imageSrcPattern.matcher(imageUrl);
        if (imageSrcMatcher.matches()) {
            return imageSrcMatcher.group(1);
        }
        throw new IllegalArgumentException("Failed to get image src from " + imageUrl);
    }

}