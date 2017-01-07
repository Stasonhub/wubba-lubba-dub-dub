package com.airent.service.provider.avito;

import com.airent.config.MvcConfig;
import com.airent.model.Advert;
import com.airent.model.Photo;
import com.airent.model.User;
import com.airent.service.LocationService;
import com.airent.service.PhotoService;
import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.RawAdvert;
import com.airent.service.provider.http.JSoupConnector;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.airent.service.provider.common.Util.getNumberInsideOf;
import static com.airent.service.provider.common.Util.removeAvitoSign;

@Component
public class AvitoProvider implements AdvertsProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int MAX_PAGES = 20;

    private static final String MAIN_PAGE_URL = "https://www.avito.ru/kazan/kvartiry/sdam/na_dlitelnyy_srok";
    private static final String PAGE_PREFIX = "?p=";

    private Pattern imageUrlPattern = Pattern.compile(".*background-image:[ ]*url[ ]*\\(//(.*)\\).*");

    private JSoupConnector jsoupConnector;
    private LocationService locationService;
    private PhoneParser phoneParser;
    private PhotoService photoService;
    private AvitoDateFormatter avitoDateFormatter;

    private String storagePath;
    private int maxItems;

    @Autowired
    public AvitoProvider(
            JSoupConnector jsoupConnector,
            LocationService locationService,
            PhoneParser phoneParser,
            PhotoService photoService,
            AvitoDateFormatter avitoDateFormatter,
            @Value("${avito.provider.max.items}") int maxItems,
            @Value("${external.storage.path}") String storagePath) {
        this.jsoupConnector = jsoupConnector;
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
            c1:
            for (int i = 0; i < MAX_PAGES; i++) {
                List<Pair<String, Long>> advertIdCollector = new ArrayList<>();

                String url = MAIN_PAGE_URL + (i != 0 ? PAGE_PREFIX + i : "");

                Document doc = jsoupConnector
                        .connect(url)
                        .get();
                Elements itemsOnPage = doc.select(".item");

                if (itemsOnPage.isEmpty()) {
                    throw new IllegalStateException("Get empty page. Page url: " + url + ". Doc: " + doc.text());
                }

                for (Element item : itemsOnPage) {
                    long itemTimestamp = getTimestamp(item);
                    if (itemTimestamp <= (timestamp + 60_000) || advertIdCollector.size() >= maxItems) {
                        // we have reached previous scan point or limits
                        break c1;
                    }


                    logger.info("Found new advert {}", getId(item));

                    RawAdvert advert = getAdvert(getId(item), itemTimestamp);
                    if (advert != null) {
                        result.add(advert);
                    }
                }
            }
        } catch (HttpStatusException httpStatusException) {
            logger.error("Http conneciton failure in parsing Status:[{}] Url:[{}] ", httpStatusException.getStatusCode(),
                    httpStatusException.getUrl(), httpStatusException);
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
        Connection.Response advertPageResponse = jsoupConnector
                .connect("https://www.avito.ru" + itemId)
                .method(Connection.Method.GET)
                .execute();

        Map<String, String> advertPageCookies = advertPageResponse.cookies();

        Document advertDocument = advertPageResponse.parse();

        /** advert */
        Elements itemViewMain = advertDocument.select(".item-view-main");
        Iterator<Element> mainParamsIterator =
                itemViewMain.select(".item-params").select(".item-params-list-item").iterator();

        Integer roomsCount = getNumberInsideOf(mainParamsIterator.next().text());
        if (roomsCount == null) {
            roomsCount = 0;
        }

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
        advert.setPrice(price);

        /** user */
        Elements contacts = advertDocument.select(".item-view-contacts");
        boolean realtor = "агентство".equals(itemViewMain.select(".seller-info-label").text().trim().toLowerCase());
        String userName = realtor ? "" : contacts.select(".seller-info-name").text().trim();
        long phone = phoneParser.getPhone(advertDocument, itemId, advertPageCookies);

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
                    jsoupConnector.connect("http://" + imageUrl.replace("80x60", "640x480"))
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


    // переписать все на Selenium WebDriver!

    //curl 'https://www.avito.ru/kazan/kvartiry/sdam/na_dlitelnyy_srok' -H 'Accept-Encoding: gzip, deflate, sdch, br' -H 'Accept-Language: en-US,en;q=0.8' -H 'Upgrade-Insecure-Requests: 1' -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8' -H 'Referer: https://www.avito.ru/kazan/kvartiry/sdam' -H 'Cookie: sessid=ea2dc5bcc19fc9e2ef6a64f8ae23aaa3.1483818549; dfp_group=94; u=2659g6rx.oy24iv.fbiozrmif0; f=3.c5237dbd1f98b87ce1acaf12057c978aace8997c08c9bba0ace8997c08c9bba0ace8997c08c9bba0ace8997c08c9bba050876fb0c4071aa881c2bc63f808a1a281c2bc63f808a1a281c2bc63f808a1a281c2bc63f808a1a281c2bc63f808a1a27ea92e9770db9ba5c6eb03ee88c7d5417ea92e9770db9ba5fd438954bf162c86e495218d85f315863f93ce1a5e56050c3bb6573363c911dfe088af55bf3bf7d1f2c876e2936792547ea92e9770db9ba5084616dd67b72570c6eb03ee88c7d541b04559a1745744cffd438954bf162c86a73e835b7a14520ceaaab7daa5371cc1b04559a1745744cfb04559a1745744cffd438954bf162c86a2a09263fde272f8084616dd67b7257013ffe65ce40d6ac43ccebdf91e27a20c73abab490aa6a91573b546961657be9363987d976878bff38ec6dd0214e93ea1d65564ebe232950d82ecd09370ddcc4d5d8260ee5b694760c45cf33093ee5edba2a09263fde272f87ea92e9770db9ba57ea92e9770db9ba55e537113157689aeaf6bec5995250c39bbb611c06be60bc740fdaa646af55584; __gads=ID=cd704713bf1a556d:T=1483818582:S=ALNI_MaF6BwxD4Ao7Bi6LIYjIj4nxk445w; _ym_uid=148381858238805046; _ym_isad=2; __tgx=1; v=1483818549; __utmt=1; __utma=99926606.280463857.1483818582.1483818582.1483818582.1; __utmb=99926606.9.9.1483819366917; __utmc=99926606; __utmz=99926606.1483818582.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); crtg_rta=' -H 'Connection: keep-alive' --compressed
    //curl 'https://www.avito.ru/kazan/kvartiry/sdam/na_dlitelnyy_srok' -H 'Accept-Encoding: gzip, deflate, sdch, br' -H 'Accept-Language: en-US,en;q=0.8' -H 'Upgrade-Insecure-Requests: 1' -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8' -H 'Referer: https://www.avito.ru/kazan/kvartiry/sdam' -H 'Cookie: sessid=ea2dw5bcc19fc9e2ef6a64f8ae23a1233.1483818549; dfp_group=93; u=2622g6rx.oy24iv.f1iozrmif0; f=3.c5237dbd1f98b87ce1acaf1205qwewrqweace8997c08c9bba0ace8997c08c9bba0ace8997c08c9bba0ace8997c08c9bba050876fb0c4071aa881c2bc63f808a1a281c2bc63f808a1a281c2bc63f808a1a281c2bc63f808a1a281c2bc63f808a1a27ea92e9770db9ba5c6eb03ee88c7d5417ea92e9770db9ba5fd438954bf162c86e495218d85f315863f93ce1a5e56050c3bb6573363c911dfe088af55bf3bf7d1f2c876e2936792547ea92e9770db9ba5084616dd67b72570c6eb03ee88c7d541b04559a1745744cffd438954bf162c86a73e835b7a14520ceaaab7daa5371cc1b04559a1745744cfb04559a1745744cffd438954bf162c86a2a09263fde272f8084616dd67b7257013ffe65ce40d6ac43ccebdf91e27a20c73abab490aa6a91573b546961657be9363987d976878bff38ec6dd0214e93ea1d65564ebe232950d82ecd09370ddcc4d5d8260ee5b694760c45cf33093ee5edba2a09263fde272f87ea92e9770db9ba57ea92e9770db9ba55e537113157689aeaf6bec5995250c39bbb611c06be60bc740fdaa646af55584;' -H 'Connection: keep-alive' --compressed

    /**
     * sessid=ea2dc5bcc19fc9e2ef6a64f8ae23aba3.1483818549
     * dfp_group=93
     * u=2659g6rx.oy24iv.f1iozrmif0
     * f=3.c5237dbd1f98b87ce1acaf12057c978aace8997c08c9bba0ace8997c08c9bba0ace8997c08c9bba0ace8997c08c9bba050876fb0c4071aa881c2bc63f808a1a281c2bc63f808a1a281c2bc63f808a1a281c2bc63f808a1a281c2bc63f808a1a27ea92e9770db9ba5c6eb03ee88c7d5417ea92e9770db9ba5fd438954bf162c86e495218d85f315863f93ce1a5e56050c3bb6573363c911dfe088af55bf3bf7d1f2c876e2936792547ea92e9770db9ba5084616dd67b72570c6eb03ee88c7d541b04559a1745744cffd438954bf162c86a73e835b7a14520ceaaab7daa5371cc1b04559a1745744cfb04559a1745744cffd438954bf162c86a2a09263fde272f8084616dd67b7257013ffe65ce40d6ac43ccebdf91e27a20c73abab490aa6a91573b546961657be9363987d976878bff38ec6dd0214e93ea1d65564ebe232950d82ecd09370ddcc4d5d8260ee5b694760c45cf33093ee5edba2a09263fde272f87ea92e9770db9ba57ea92e9770db9ba55e537113157689aeaf6bec5995250c39bbb611c06be60bc740fdaa646af55584
     *
     *
     *
     */

}