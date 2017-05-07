package service.provider.totook;

import service.provider.api.AdvertsProvider;
import service.provider.api.ParsedAdvert;
import service.provider.api.ParsedAdvertHeader;
import service.provider.connection.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static service.provider.common.Util.getLongNumberInsideOf;
import static service.provider.common.Util.getNumberInsideOf;

@Singleton
public class TotookAdvertsProvider implements AdvertsProvider {

    private Logger logger = LoggerFactory.getLogger(TotookAdvertsProvider.class);

    private int MAX_PAGES = 10;
    private Pattern headerPattern = Pattern.compile(".*([0-9])-комн. квартира, ([0-9]+) м");
    private Pattern coordinatesPattern = Pattern.compile(".*coordinates: \\[([0-9]+.[0-9]+), ([0-9]+.[0-9]+)\\].*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    private OkHttpClient okHttpClient;
    private TotookDateFormatter totookDateFormatter;
    private int maxItemsToScan;

    @Inject
    public TotookAdvertsProvider(OkHttpClient okHttpClient,
                                 TotookDateFormatter totookDateFormatter,
                                 @Named("totook.max.items") int maxItemsToScan) {
        this.okHttpClient = okHttpClient;
        this.totookDateFormatter = totookDateFormatter;
        this.maxItemsToScan = maxItemsToScan;
    }

    @Override
    public String getType() {
        return "TTK";
    }

    @Override
    public boolean isVerifier() {
        return true;
    }

    @Override
    public Iterator<ParsedAdvertHeader> getHeaders() {
        return new Iterator<ParsedAdvertHeader>() {

            int pageNumber = 0;
            Iterator<ParsedAdvertHeader> currentPageHeaders;

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

                    pageNumber++;
                    Document document = doGet("http://kazan.totook.ru/catalog/?PARENT_SECTION=28&type=1&price_from=0&price_to=100000&period=4&PAGEN_1=" + pageNumber);
                    currentPageHeaders = document.select(".b-catalog__object")
                            .stream()
                            .filter(v -> !v.hasClass("access"))
                            .map(header -> {
                                ParsedAdvertHeader parsedAdvertHeader = new ParsedAdvertHeader();
                                parsedAdvertHeader.setAdvertUrl("http://kazan.totook.ru" + header.select("a").attr("href"));
                                parsedAdvertHeader.setPublicationTimestamp(totookDateFormatter.getTimestamp(
                                        header.select(".b-catalog__object__date").text()));
                                return parsedAdvertHeader;
                            }).collect(Collectors.toList()).iterator();

                    logger.info("Spend time for headers page opening {} : {} ms", pageNumber, System.currentTimeMillis() - startTime);

                }

                if (!currentPageHeaders.hasNext()) {
                    logger.error("Iterator has no next element.");
                }

                return currentPageHeaders.next();
            }
        };
    }

    @Override
    public ParsedAdvert getAdvert(ParsedAdvertHeader parsedAdvertHeader) {
        long startTime = System.currentTimeMillis();
        Document advertPage = doGet(parsedAdvertHeader.getAdvertUrl());
        logger.info("Spend time for opening advert {} : {} ms", parsedAdvertHeader.getAdvertUrl(), System.currentTimeMillis() - startTime);

        // address/sq/price/coordinates/phone
        String address = advertPage.select(".b-detail__address").text();
        Integer sq = getSqFromHeader(advertPage.select(".main-inner h1").text());
        Integer price = getNumberInsideOf(advertPage.select(".b-detail__description__price__summ").text());
        Pair<Double, Double> coordinates = getCoordinates(advertPage.select(".b-detail__map + script")
                .iterator().next().data());
        long phone = getLongNumberInsideOf(advertPage.select(".b-detail__phone-button__phone").text().substring(2));

        ParsedAdvert parsedAdvert = new ParsedAdvert();
        parsedAdvert.setAddress(address);
        parsedAdvert.setPublicationTimestamp(parsedAdvertHeader.getPublicationTimestamp());
        parsedAdvert.setSq(sq);
        parsedAdvert.setPrice(price);
        parsedAdvert.setLatitude(coordinates.getLeft());
        parsedAdvert.setLongitude(coordinates.getRight());
        parsedAdvert.setPhone(phone);
        parsedAdvert.setTrustRate(20_000);

        return parsedAdvert;
    }

    @Override
    public int getMaxItemsToScan() {
        return maxItemsToScan;
    }

    private Document doGet(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            String html = okHttpClient.get().newCall(request)
                    .execute()
                    .body()
                    .string();
            return Jsoup.parse(html);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getSqFromHeader(String headerText) {
        Matcher headerMatcher = headerPattern.matcher(headerText);
        if (headerMatcher.find()) {
            return Integer.valueOf(headerMatcher.group(2));
        }
        throw new IllegalArgumentException("Failed to retrieve sq from " + headerText);
    }

    Pair<Double, Double> getCoordinates(String coordinatesText) {
        Matcher coordinatesMatcher = coordinatesPattern.matcher(coordinatesText);
        if (coordinatesMatcher.matches()) {
            return Pair.of(Double.valueOf(coordinatesMatcher.group(2)), Double.valueOf(coordinatesMatcher.group(1)));
        }
        throw new IllegalArgumentException("Failed to get coordinates from " + coordinatesText);
    }
}