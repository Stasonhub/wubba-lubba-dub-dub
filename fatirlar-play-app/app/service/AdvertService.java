package service;

import model.Advert;
import model.District;
import model.rest.SearchRequest;
import model.ui.AdvertPrices;
import repository.AdvertRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.*;

@Singleton
public class AdvertService {

    private static final int ADVERTS_PER_REQUEST = 15;
    private static final int ADVERTS_ON_MAIN_PAGE = 9;

    @Inject
    private AdvertRepository advertMapper;

    public List<Advert> getAdvertsForMainPage() {
        return advertMapper.getNextAdvertsBeforeTime(System.currentTimeMillis(), ADVERTS_ON_MAIN_PAGE);
    }

    public int getPagesCount(SearchRequest searchRequest) {
        Objects.requireNonNull(searchRequest.getPriceRange());

        if (searchRequest.getPriceRange().size() != 2) {
            throw new IllegalStateException("Incorrect price range");
        }

        Collection<District> districts = searchRequest.getDistricts();
        if (districts == null || districts.isEmpty()) {
            districts = EnumSet.allOf(District.class);
        }

        int priceFrom = searchRequest.getPriceRange().get(0) * 1000;
        int priceTo = searchRequest.getPriceRange().get(1) * 1000;

        List<Integer> rooms = new ArrayList<>();
        if (searchRequest.isRoom1()) {
            rooms.add(1);
        }
        if (searchRequest.isRoom2()) {
            rooms.add(2);
        }
        if (searchRequest.isRoom3()) {
            rooms.add(3);
        }

        int advertsCount = advertMapper.getAdvertsCount(districts, priceFrom, priceTo, rooms);
        return advertsCount / ADVERTS_PER_REQUEST;
    }

    public List<Advert> getAdverts(SearchRequest searchRequest) {
        Objects.requireNonNull(searchRequest.getPriceRange());

        if (searchRequest.getPage() < 0) {
            throw new IllegalArgumentException("Illegal page number");
        }

        if (searchRequest.getPriceRange().size() != 2) {
            throw new IllegalStateException("Incorrect price range");
        }

        Collection<District> districts = searchRequest.getDistricts();
        if (districts == null || districts.isEmpty()) {
            districts = EnumSet.allOf(District.class);
        }

        int priceFrom = searchRequest.getPriceRange().get(0) * 1000;
        int priceTo = searchRequest.getPriceRange().get(1) * 1000;

        List<Integer> rooms = new ArrayList<>();
        if (searchRequest.isRoom1()) {
            rooms.add(1);
        }
        if (searchRequest.isRoom2()) {
            rooms.add(2);
        }
        if (searchRequest.isRoom3()) {
            rooms.add(3);
        }

        return advertMapper.getAdverts(districts, priceFrom, priceTo, rooms, searchRequest.getPage() * ADVERTS_PER_REQUEST, ADVERTS_PER_REQUEST);
    }

    public Advert getAdvert(long id) {
        return advertMapper.findById(id);
    }

    public AdvertPrices getAdvertPrices() {
        return advertMapper.getAdvertPrices();
    }

    public void removeAdvert(long advertId) {
        advertMapper.deleteAdvert(advertId);
    }

}
