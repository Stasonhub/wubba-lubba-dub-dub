package com.airent.model.ui;

public class SearchBoxState {

    private boolean avSelected;
    private boolean vhSelected;
    private boolean krSelected;
    private boolean msSelected;
    private boolean nsSelected;
    private boolean pvSelected;
    private boolean cvSelected;

    private boolean rooms1Pressed;
    private boolean rooms2Pressed;
    private boolean rooms3Pressed;

    private int priceFrom;
    private int priceTo;

    private int priceMin;
    private int priceMax;


    public boolean isAvSelected() {
        return avSelected;
    }

    public SearchBoxState setAvSelected(boolean avSelected) {
        this.avSelected = avSelected;
        return this;
    }

    public boolean isVhSelected() {
        return vhSelected;
    }

    public SearchBoxState setVhSelected(boolean vhSelected) {
        this.vhSelected = vhSelected;
        return this;
    }

    public boolean isKrSelected() {
        return krSelected;
    }

    public SearchBoxState setKrSelected(boolean krSelected) {
        this.krSelected = krSelected;
        return this;
    }

    public boolean isMsSelected() {
        return msSelected;
    }

    public SearchBoxState setMsSelected(boolean msSelected) {
        this.msSelected = msSelected;
        return this;
    }

    public boolean isNsSelected() {
        return nsSelected;
    }

    public SearchBoxState setNsSelected(boolean nsSelected) {
        this.nsSelected = nsSelected;
        return this;
    }

    public boolean isPvSelected() {
        return pvSelected;
    }

    public SearchBoxState setPvSelected(boolean pvSelected) {
        this.pvSelected = pvSelected;
        return this;
    }

    public boolean isCvSelected() {
        return cvSelected;
    }

    public SearchBoxState setCvSelected(boolean cvSelected) {
        this.cvSelected = cvSelected;
        return this;
    }

    public boolean isRooms1Pressed() {
        return rooms1Pressed;
    }

    public SearchBoxState setRooms1Pressed(boolean rooms1Pressed) {
        this.rooms1Pressed = rooms1Pressed;
        return this;
    }

    public boolean isRooms2Pressed() {
        return rooms2Pressed;
    }

    public SearchBoxState setRooms2Pressed(boolean rooms2Pressed) {
        this.rooms2Pressed = rooms2Pressed;
        return this;
    }

    public boolean isRooms3Pressed() {
        return rooms3Pressed;
    }

    public SearchBoxState setRooms3Pressed(boolean rooms3Pressed) {
        this.rooms3Pressed = rooms3Pressed;
        return this;
    }

    public int getPriceFrom() {
        return priceFrom;
    }

    public SearchBoxState setPriceFrom(int priceFrom) {
        this.priceFrom = priceFrom;
        return this;
    }

    public int getPriceTo() {
        return priceTo;
    }

    public SearchBoxState setPriceTo(int priceTo) {
        this.priceTo = priceTo;
        return this;
    }

    public int getPriceMin() {
        return priceMin;
    }

    public void setPriceMin(int priceMin) {
        this.priceMin = priceMin;
    }

    public int getPriceMax() {
        return priceMax;
    }

    public void setPriceMax(int priceMax) {
        this.priceMax = priceMax;
    }
}
