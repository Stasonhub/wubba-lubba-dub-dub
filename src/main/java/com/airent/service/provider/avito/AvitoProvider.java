package com.airent.service.provider.avito;

import com.airent.model.Advert;
import com.airent.model.Photo;
import com.airent.model.User;
import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.RawAdvert;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AvitoProvider implements AdvertsProvider {

    private static final int MAX_PAGES = 20;

    private static final int SIGN_HEIGHT = 40;

    private static final String MAIN_PAGE_URL = "https://www.avito.ru/kazan/kvartiry/sdam/na_dlitelnyy_srok?p=";

    private static final String USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36";

    private static AvitoDateFormatter avitoDateFormatter = new AvitoDateFormatter();

    private Pattern imageUrlPattern = Pattern.compile(".*background-image:[ ]*url[ ]*\\(//(.*)\\).*");

    private PhoneParser phoneParser;

    private String storagePath;

    private int maxItems;

    @Autowired
    public AvitoProvider(PhoneParser phoneParser, @Value("${avito.provider.max.items}") int maxItems,
                         @Value("${avito.provider.storage.path}") String storagePath) {
        this.phoneParser = phoneParser;
        this.maxItems = maxItems;
        this.storagePath = storagePath;
    }

    @Override
    public String getType() {
        return "AV";
    }

    @Override
    public List<RawAdvert> getAdvertsUntil(long timestamp) {
        List<RawAdvert> result = new ArrayList<>();
        try {
            List<Pair<String, Long>> advertIdCollector = new ArrayList<>();

            c1:
            for (int i = 0; i < MAX_PAGES; i++) {
                Document doc = Jsoup.connect(MAIN_PAGE_URL + i).userAgent(USER_AGENT).get();
                Elements itemsOnPage = doc.select(".item");
                for (Element item : itemsOnPage) {
                    long itemTimestamp = getTimestamp(item);
                    if (itemTimestamp <= timestamp || advertIdCollector.size() >= maxItems) {
                        // we have reached previous scan point or limits
                        break c1;
                    }

                    advertIdCollector.add(Pair.of(getId(item), itemTimestamp));
                }
            }

            for (Pair<String, Long> item : advertIdCollector) {
                result.add(getAdvert(item.getLeft(), item.getRight()));
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        Document advertDocument = Jsoup.connect("https://www.avito.ru" + itemId).userAgent(USER_AGENT).get();

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
                itemViewMain.select(".item-view-map").select(".item-map-address").select(".streetAddress").text();

        String description = itemViewMain.select(".item-description-text p").text();

        Advert advert = new Advert();
        advert.setBedrooms(1);
        advert.setRooms(roomsCount);
        advert.setSq(sq);
        advert.setFloor(floor);
        advert.setMaxFloor(maxFloor);
        advert.setAddress(address);
        advert.setDescription(description);
        advert.setPublicationDate(timestamp);

        /** user */
        Elements contacts = advertDocument.select(".item-view-contacts");
        boolean realtor = "агентство".equals(itemViewMain.select(".seller-info-label").text().trim().toLowerCase());
        String userName = realtor ? "" : contacts.select(".seller-info-name").text().trim();
        long phone = phoneParser.getPhone(advertDocument, itemId);

        User user = new User();
        user.setName(userName);
        user.setPhone(phone);

        /** photos */
        List<Photo> photos = new ArrayList<>();
        Elements imageLinks = advertDocument.select(".item-view-gallery").select(".gallery-list-item-link");

        long photosPathId = System.currentTimeMillis();

        int index = 0;
        for (Element imageLink : imageLinks) {
            String imageUrl = getImageUrl(imageLink.attr("style"));
            Connection.Response response =
                    Jsoup.connect("http://" + imageUrl.replace("80x60", "640x480")).userAgent(USER_AGENT)
                            .ignoreContentType(true).execute();

            String path = storagePath + File.separator + photosPathId + File.separator + index + ".jpg";
            new File(path).getParentFile().mkdirs();

            BufferedImage bufferedImage =
                    removeAvitoSign(ImageIO.read(new ByteArrayInputStream(response.bodyAsBytes())));
            try (FileOutputStream out = new FileOutputStream(new java.io.File(path))) {
                ImageIO.write(bufferedImage, "jpeg", out);
            }

            Photo photo = new Photo();
            photo.setPath(path);
            photo.setMain(index == 0);

            index++;
        }

        RawAdvert rawAdvert = new RawAdvert();
        rawAdvert.setAdvert(advert);
        rawAdvert.setUser(user);
        rawAdvert.setPhotos(photos);
        return rawAdvert;
    }

    private BufferedImage removeAvitoSign(BufferedImage originalImage) {
        int height = originalImage.getHeight() - SIGN_HEIGHT;
        return originalImage.getSubimage(0, 0, originalImage.getWidth(), height);
    }

    String getImageUrl(String fullImageUrl) {
        Matcher matcher = imageUrlPattern.matcher(fullImageUrl);
        if (!matcher.matches()) {
            throw new IllegalStateException("Failed to retrieve image from " + fullImageUrl);
        }
        return matcher.group(1);
    }

    private int getNumberInsideOf(String val) {
        StringBuilder result = new StringBuilder();
        boolean collecting = false;
        for (char c : val.toCharArray()) {
            if (c >= '0' && c <= '9') {
                result.append(c);
            } else {
                if (collecting) {
                    break;
                }
                collecting = false;
            }
        }
        if (result.length() == 0) {
            throw new IllegalArgumentException("There is no number in: " + val);
        }
        return Integer.parseInt(result.toString());
    }

    private static class AvitoDateFormatter {

        private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

        long getTimestamp(String date) {
            return parseDateTime(toStrictFormat(date)).toInstant(ZoneOffset.UTC).toEpochMilli();
        }

        String toStrictFormat(String date) {
            String dateFormatted = date.toLowerCase().trim();
            if (dateFormatted.startsWith("сегодня")) {
                int dayOfMonth = LocalDate.now().getDayOfMonth();
                int monthValue = LocalDate.now().getMonthValue();
                return String.format("%s %02d.%02d.%d", dateFormatted.split(" ")[1], dayOfMonth, monthValue,
                        LocalDate.now().getYear());
            } else if (dateFormatted.startsWith("вчера")) {
                int dayOfMonth = LocalDate.now().minusDays(1).getDayOfMonth();
                int monthValue = LocalDate.now().minusDays(1).getMonthValue();
                return String.format("%s %02d.%02d.%d", dateFormatted.split(" ")[1], dayOfMonth, monthValue,
                        LocalDate.now().getYear());
            }

            String[] parts = dateFormatted.split(" ");
            return String.format("%s %s.%s.%d", parts[2], parts[0], replaceMonth(parts[1]), LocalDate.now().getYear());
        }

        private String replaceMonth(String monthString) {
            if (monthString.startsWith("янв")) {
                return "01";
            }
            if (monthString.startsWith("фев")) {
                return "02";
            }
            if (monthString.startsWith("март")) {
                return "03";
            }
            if (monthString.startsWith("апр")) {
                return "04";
            }
            if (monthString.startsWith("ма")) {
                return "05";
            }
            if (monthString.startsWith("июн")) {
                return "06";
            }
            if (monthString.startsWith("июл")) {
                return "07";
            }
            if (monthString.startsWith("авг")) {
                return "08";
            }
            if (monthString.startsWith("сент")) {
                return "09";
            }
            if (monthString.startsWith("окт")) {
                return "10";
            }
            if (monthString.startsWith("ноя")) {
                return "11";
            }
            if (monthString.startsWith("дек")) {
                return "12";
            }
            throw new IllegalArgumentException("Unknown month: " + monthString);
        }

        LocalDateTime parseDateTime(String dateTimeText) {
            return LocalDateTime.parse(dateTimeText, dateTimeFormatter);
        }
    }

}