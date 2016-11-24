package com.airent.service.provider.avito;

import com.airent.model.Advert;
import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.RawAdvert;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AvitoProvider implements AdvertsProvider {

    private static final int MAX_PAGES = 3;
    private static final String MAIN_PAGE_URL = "https://www.avito.ru/kazan/kvartiry/sdam/na_dlitelnyy_srok?p=";
    private static AvitoDateFormatter avitoDateFormatter = new AvitoDateFormatter();
    private PhoneParser phoneParser = new PhoneParser();

    @Override
    public List<RawAdvert> getAdvertsUntil(long timestamp) {
        List<RawAdvert> result = new ArrayList<>();
        try {
            List<Pair<String, Long>> advertIdCollector = new ArrayList<>();

            for (int i = 0; i < MAX_PAGES; i++) {
                Document doc = Jsoup.connect(MAIN_PAGE_URL + i).get();
                Elements itemsOnPage = doc.select(".item");
                for (Element item : itemsOnPage) {
                    long itemTimestamp = getTimestamp(item);
                    if (itemTimestamp <= timestamp) {
                        // we have reached previous scan point
                        break;
                    }

                    advertIdCollector.add(Pair.of(getId(item), itemTimestamp));
                }

                for (Pair<String, Long> item : advertIdCollector) {
                    result.add(getAdvert(item.getLeft(), item.getRight()));
                }
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
        Document advertDocument = Jsoup.connect("https://www.avito.ru" + itemId).get();

        /** advert */
        Elements itemViewMain = advertDocument.select(".item-view-main");
        Iterator<Element> mainParamsIterator = itemViewMain
                .select(".item-params")
                .select(".item-params-list-item")
                .iterator();

        int roomsCount = getNumberInsideOf(mainParamsIterator.next().text());
        int floor = getNumberInsideOf(mainParamsIterator.next().text());
        int maxFloor = getNumberInsideOf(mainParamsIterator.next().text());
        mainParamsIterator.next().text(); // house type
        int sq = getNumberInsideOf(mainParamsIterator.next().text());

        String address = itemViewMain
                .select(".item-view-map")
                .select(".item-map-address")
                .select(".streetAddress")
                .text();

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
        Elements contacts = itemViewMain.select(".item-view-contacts");
        boolean realtor = "агентство".equals(itemViewMain.select(".seller-info-label").text().toLowerCase());
        String userName = realtor ? "" : contacts.select(".seller-info-name").text();
        long phone = phoneParser.getPhone(advertDocument, itemId);


        RawAdvert rawAdvert = new RawAdvert();
        rawAdvert.setAdvert(advert);
        return rawAdvert;
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
                return String.format("%s %d.%d.%d", dateFormatted.split(" ")[1], dayOfMonth, monthValue, LocalDate.now().getYear());
            } else if (dateFormatted.startsWith("вчера")) {
                int dayOfMonth = LocalDate.now().minusDays(1).getDayOfMonth();
                int monthValue = LocalDate.now().minusDays(1).getMonthValue();
                return String.format("%s %d.%d.%d", dateFormatted.split(" ")[1], dayOfMonth, monthValue, LocalDate.now().getYear());
            }

            String[] parts = dateFormatted.split(" ");
            return String.format("%s %s.%s.%d", parts[2], parts[0], replaceMonth(parts[1]), LocalDate.now().getYear());
        }

        private String replaceMonth(String monthString) {
            if (monthString.startsWith("янв"))
                return "1";
            if (monthString.startsWith("фев"))
                return "2";
            if (monthString.startsWith("март"))
                return "3";
            if (monthString.startsWith("апр"))
                return "4";
            if (monthString.startsWith("ма"))
                return "5";
            if (monthString.startsWith("июн"))
                return "6";
            if (monthString.startsWith("июл"))
                return "7";
            if (monthString.startsWith("авг"))
                return "8";
            if (monthString.startsWith("сент"))
                return "9";
            if (monthString.startsWith("окт"))
                return "10";
            if (monthString.startsWith("ноя"))
                return "11";
            if (monthString.startsWith("дек"))
                return "12";
            throw new IllegalArgumentException("Unknown month: " + monthString);
        }


        LocalDateTime parseDateTime(String dateTimeText) {
            return LocalDateTime.parse(dateTimeText, dateTimeFormatter);
        }
    }

}