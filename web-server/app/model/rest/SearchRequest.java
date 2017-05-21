package model.rest;

import model.District;
import model.ui.AdvertPrices;
import model.ui.SearchBoxState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchRequest {
    private List<District> districts;
    private List<Integer> priceRange;
    private boolean room1;
    private boolean room2;
    private boolean room3;
    private int page;

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
    }

    public boolean isRoom1() {
        return room1;
    }

    public void setRoom1(boolean room1) {
        this.room1 = room1;
    }

    public boolean isRoom2() {
        return room2;
    }

    public void setRoom2(boolean room2) {
        this.room2 = room2;
    }

    public boolean isRoom3() {
        return room3;
    }

    public void setRoom3(boolean room3) {
        this.room3 = room3;
    }

    public List<Integer> getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(List<Integer> priceRange) {
        this.priceRange = priceRange;
    }

    public int getPage() {
        return page;
    }

    public SearchRequest setPage(int page) {
        this.page = page;
        return this;
    }

    public SearchRequest normalize() {
        return normalize(this);
    }

    public SearchRequest normalize(SearchRequest searchRequest) {
        SearchRequest normalized = new SearchRequest();

        normalized.setDistricts(searchRequest.getDistricts() == null ? Collections.emptyList() : searchRequest.getDistricts());

        if (!searchRequest.isRoom1() && !searchRequest.isRoom2() && !searchRequest.isRoom3()) {
            normalized.setRoom1(true);
        } else {
            normalized.setRoom1(searchRequest.isRoom1());
            normalized.setRoom2(searchRequest.isRoom2());
            normalized.setRoom3(searchRequest.isRoom3());
        }

        List<Integer> priceRange = searchRequest.getPriceRange();
        if (priceRange != null && priceRange.size() == 2 && priceRange.get(0) > 0 && priceRange.get(1) > 0 && priceRange.get(1) > priceRange.get(0)) {
            normalized.setPriceRange(priceRange);
        } else {
            List<Integer> defaultPriceRange = new ArrayList<>();
            defaultPriceRange.add(8);
            defaultPriceRange.add(60);
            normalized.setPriceRange(defaultPriceRange);
        }

        normalized.setPage(searchRequest.getPage());

        return normalized;
    }

    private SearchBoxState getSearchBoxDefaultState(AdvertPrices advertPrices) {
        SearchBoxState searchBoxState = new SearchBoxState();
        searchBoxState.setAvSelected(true);
        searchBoxState.setVhSelected(true);
        searchBoxState.setKrSelected(true);
        searchBoxState.setMsSelected(true);
        searchBoxState.setNsSelected(true);
        searchBoxState.setPvSelected(true);
        searchBoxState.setCvSelected(true);
        if (advertPrices != null) {
            searchBoxState.setPriceMin(advertPrices.getPriceMin() / 1000);
            searchBoxState.setPriceMax(advertPrices.getPriceMax() / 1000);
        } else {
            searchBoxState.setPriceMin(1);
            searchBoxState.setPriceMax(120);
        }
        searchBoxState.setPriceFrom(searchBoxState.getPriceMin());
        searchBoxState.setPriceTo(searchBoxState.getPriceMax());
        return searchBoxState;
    }

    private SearchBoxState getSearchBoxState(SearchRequest searchRequest, AdvertPrices advertPrices) {
        SearchBoxState searchBoxState = new SearchBoxState();
        searchBoxState.setAvSelected(searchRequest.getDistricts().contains(District.AV));
        searchBoxState.setVhSelected(searchRequest.getDistricts().contains(District.VH));
        searchBoxState.setKrSelected(searchRequest.getDistricts().contains(District.KR));
        searchBoxState.setMsSelected(searchRequest.getDistricts().contains(District.MS));
        searchBoxState.setNsSelected(searchRequest.getDistricts().contains(District.NS));
        searchBoxState.setPvSelected(searchRequest.getDistricts().contains(District.PV));
        searchBoxState.setCvSelected(searchRequest.getDistricts().contains(District.CV));
        searchBoxState.setRooms1Pressed(searchRequest.isRoom1());
        searchBoxState.setRooms2Pressed(searchRequest.isRoom2());
        searchBoxState.setRooms3Pressed(searchRequest.isRoom3());
        searchBoxState.setPriceMin(advertPrices.getPriceMin() / 1000);
        searchBoxState.setPriceMax(advertPrices.getPriceMax() / 1000);
        searchBoxState.setPriceFrom(Math.max(searchBoxState.getPriceMin(), searchRequest.getPriceRange().get(0)));
        searchBoxState.setPriceTo(Math.min(searchBoxState.getPriceMax(), searchRequest.getPriceRange().get(1)));
        return searchBoxState;
    }
}
