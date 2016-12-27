package com.airent.service.provider.avito;

import com.airent.config.MvcConfig;
import com.airent.model.Advert;
import com.airent.model.Photo;
import com.airent.model.User;
import com.airent.service.LocationService;
import com.airent.service.PhotoService;
import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.RawAdvert;
import com.airent.service.provider.http.JSoupTorConnector;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Connection;
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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.airent.service.provider.common.Util.getNumberInsideOf;
import static com.airent.service.provider.common.Util.removeAvitoSign;

@Component
public class AvitoProvider implements AdvertsProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int TIMEOUT = 20_000;

    private static final int MAX_PAGES = 20;

    private static final String MAIN_PAGE_URL = "https://www.avito.ru/kazan/kvartiry/sdam/na_dlitelnyy_srok?p=";

    private Pattern imageUrlPattern = Pattern.compile(".*background-image:[ ]*url[ ]*\\(//(.*)\\).*");

    private JSoupTorConnector jsoupTorConnector;
    private LocationService locationService;
    private PhoneParser phoneParser;
    private PhotoService photoService;
    private AvitoDateFormatter avitoDateFormatter;

    private String storagePath;
    private int maxItems;

    @Autowired
    public AvitoProvider(
            JSoupTorConnector jsoupTorConnector,
            LocationService locationService,
            PhoneParser phoneParser,
            PhotoService photoService,
            AvitoDateFormatter avitoDateFormatter,
            @Value("${avito.provider.max.items}") int maxItems,
            @Value("${external.storage.path}") String storagePath) {
        this.jsoupTorConnector = jsoupTorConnector;
        this.locationService = locationService;
        this.phoneParser = phoneParser;
        this.photoService = photoService;
        this.avitoDateFormatter = avitoDateFormatter;
        this.maxItems = maxItems;
        this.storagePath = storagePath;
    }

    @Override
    public String getType() {
        return "AVT";
    }

    @Override
    public List<RawAdvert> getAdvertsUntil(long timestamp) {
        List<RawAdvert> result = new ArrayList<>();
        try {
            List<Pair<String, Long>> advertIdCollector = new ArrayList<>();

            c1:
            for (int i = 0; i < MAX_PAGES; i++) {
                Document doc = jsoupTorConnector.connect(MAIN_PAGE_URL + i)
                        .get();
                Elements itemsOnPage = doc.select(".item");
                for (Element item : itemsOnPage) {
                    long itemTimestamp = getTimestamp(item);
                    if (itemTimestamp <= (timestamp + 60_000) || advertIdCollector.size() >= maxItems) {
                        // we have reached previous scan point or limits
                        break c1;
                    }

                    advertIdCollector.add(Pair.of(getId(item), itemTimestamp));
                }
            }

            for (Pair<String, Long> item : advertIdCollector) {
                RawAdvert advert = getAdvert(item.getLeft(), item.getRight());
                if (advert != null) {
                    result.add(advert);
                }
            }
        } catch (IOException e) {
            logger.error("Failure in parsing", e);
        }
        return result;
    }

    private String getId(Element item) {
        return item.select(".item-description-title-link").attr("href");
    }

    private long getTimestamp(Element item) {
        return avitoDateFormatter.getTimestamp(item.select(".date").text());
    }

    private RawAdvert getAdvert(String itemId, long timestamp) throws IOException {
        Document advertDocument = jsoupTorConnector.connect("https://www.avito.ru" + itemId)
                .get();

        /** advert */
        Elements itemViewMain = advertDocument.select(".item-view-main");
        Iterator<Element> mainParamsIterator =
                itemViewMain.select(".item-params").select(".item-params-list-item").iterator();

        int roomsCount = getNumberInsideOf(mainParamsIterator.next().text());
        int floor = getNumberInsideOf(mainParamsIterator.next().text());
        int maxFloor = getNumberInsideOf(mainParamsIterator.next().text());
        mainParamsIterator.next().text(); // house type
        int sq = getNumberInsideOf(mainParamsIterator.next().text());

        String address =
                itemViewMain.select(".item-view-map").select(".item-map-address").select("[itemprop=streetAddress]").text();
        Integer price = getNumberInsideOf(advertDocument.select(".item-price").select(".price-value-string").text());

        if (price == null) {
            logger.warn("Price is empty for " + itemId);
            return null;
        }

        String description = itemViewMain.select(".item-description-text p").text();


        Elements searchMap = advertDocument.select(".b-search-map");
        String latVal = searchMap.attr("data-map-lat");
        String lonVal = searchMap.attr("data-map-lon");

        if (StringUtils.isEmpty(latVal) || StringUtils.isEmpty(lonVal)) {
            logger.warn("Wrong address without coordinates for " + itemId);
            return null;
        }

        double latitude = Double.parseDouble(latVal);
        double longitude = Double.parseDouble(lonVal);

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

        /** user */
        Elements contacts = advertDocument.select(".item-view-contacts");
        boolean realtor = "агентство".equals(itemViewMain.select(".seller-info-label").text().trim().toLowerCase());
        String userName = realtor ? "" : contacts.select(".seller-info-name").text().trim();
        long phone = phoneParser.getPhone(advertDocument, itemId);

        User user = new User();
        user.setName(userName);
        user.setPhone(phone);
        user.setRegistered(false);
        user.setTrustRate(600);

        /** photos */
        List<Photo> photos = new ArrayList<>();
        Elements imageLinks = advertDocument.select(".item-view-gallery").select(".gallery-list-item-link");

        if (imageLinks.isEmpty()) {
            logger.warn("Not found photos for advert " + itemId);
            return null;
        }

        long photosPathId = System.currentTimeMillis();

        int index = 0;
        for (Element imageLink : imageLinks) {
            String imageUrl = getImageUrl(imageLink.attr("style"));
            Connection.Response response =
                    jsoupTorConnector.connect("http://" + imageUrl.replace("80x60", "640x480"))
                            .ignoreContentType(true)
                            .execute();

            String path = storagePath + File.separator + "a" + File.separator + photosPathId + File.separator + index + ".jpg";
            new File(path).getParentFile().mkdirs();

            BufferedImage bufferedImage =
                    removeAvitoSign(ImageIO.read(new ByteArrayInputStream(response.bodyAsBytes())));
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

    String getImageUrl(String fullImageUrl) {
        Matcher matcher = imageUrlPattern.matcher(fullImageUrl);
        if (!matcher.matches()) {
            throw new IllegalStateException("Failed to retrieve image from " + fullImageUrl);
        }
        return matcher.group(1);
    }


}