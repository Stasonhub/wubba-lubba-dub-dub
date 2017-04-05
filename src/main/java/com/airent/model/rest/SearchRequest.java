package com.airent.model.rest;

import com.airent.model.District;

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
}
