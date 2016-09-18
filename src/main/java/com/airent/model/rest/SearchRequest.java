package com.airent.model.rest;

import com.airent.model.District;

import java.util.List;

public class SearchRequest {
    private List<District> districts;
    private boolean rooms1;
    private boolean rooms2;
    private boolean rooms3;
    private List<Integer> priceRange;

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
    }

    public boolean isRooms1() {
        return rooms1;
    }

    public void setRooms1(boolean rooms1) {
        this.rooms1 = rooms1;
    }

    public boolean isRooms2() {
        return rooms2;
    }

    public void setRooms2(boolean rooms2) {
        this.rooms2 = rooms2;
    }

    public boolean isRooms3() {
        return rooms3;
    }

    public void setRooms3(boolean rooms3) {
        this.rooms3 = rooms3;
    }

    public List<Integer> getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(List<Integer> priceRange) {
        this.priceRange = priceRange;
    }
}
