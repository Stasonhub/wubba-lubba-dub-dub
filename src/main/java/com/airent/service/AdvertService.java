package com.airent.service;

import com.airent.mapper.AdvertMapper;
import com.airent.model.Advert;
import com.airent.model.District;
import com.airent.model.rest.SearchRequest;
import com.airent.model.ui.AdvertPrices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdvertService {

    private static final int ADVERTS_PER_REQUEST = 15;

    @Autowired
    private AdvertMapper advertMapper;

    public List<Advert> getAdvertsForMainPage() {
        return advertMapper.getNextAdvertsBeforeTime(System.currentTimeMillis(), ADVERTS_PER_REQUEST);
    }

    public List<Advert> getAdvertsForMainPageFrom(long timestamp) {
        return advertMapper.getNextAdvertsBeforeTime(timestamp, ADVERTS_PER_REQUEST);
    }

    public List<Advert> searchAdvertsUntilTime(SearchRequest searchRequest, long timestamp) {
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
        if (searchRequest.isRooms1()) {
            rooms.add(1);
        }
        if (searchRequest.isRooms2()) {
            rooms.add(2);
        }
        if (searchRequest.isRooms3()) {
            rooms.add(3);
        }
        if (!searchRequest.isRooms1() && !searchRequest.isRooms2() && !searchRequest.isRooms3()) {
            rooms.add(1);
            rooms.add(2);
            rooms.add(3);
        }

        return advertMapper.searchNextAdvertsBeforeTime(districts, priceFrom, priceTo, rooms, timestamp, ADVERTS_PER_REQUEST);
    }

    public Advert getAdvert(long id) {
        return advertMapper.findById(id);
    }

    public AdvertPrices getAdvertPrices() {
        return advertMapper.getAdvertPrices();
    }


    public List<Advert> getRawAdverts() {
        return advertMapper.getRawAdverts();
    }

    public void approveAdvert(long advertId) {
        advertMapper.setAdvertNotRaw(advertId);
    }

    public void removeAdvert(long advertId) {
        advertMapper.deleteAdvert(advertId);
    }

}
