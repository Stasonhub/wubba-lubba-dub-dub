package com.airent.model;

public class Advert {

    private long id;
    private long userId;
    private long publicationDate;

    private Distinct district;
    private String address;
    private int floor;
    private int maxFloor;
    private int rooms;
    private int sq;
    private int price;

    private int conditions;
    private String description;
    private String mainPhotoUrl;

    public long getId() {
        return id;
    }

    public Advert setId(long id) {
        this.id = id;
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public Advert setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public long getPublicationDate() {
        return publicationDate;
    }

    public Advert setPublicationDate(long publicationDate) {
        this.publicationDate = publicationDate;
        return this;
    }

    public Distinct getDistrict() {
        return district;
    }

    public Advert setDistrict(Distinct district) {
        this.district = district;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getMaxFloor() {
        return maxFloor;
    }

    public void setMaxFloor(int maxFloor) {
        this.maxFloor = maxFloor;
    }

    public int getRooms() {
        return rooms;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public int getSq() {
        return sq;
    }

    public void setSq(int sq) {
        this.sq = sq;
    }

    public int getPrice() {
        return price;
    }

    public Advert setPrice(int price) {
        this.price = price;
        return this;
    }

    public int getConditions() {
        return conditions;
    }

    public Advert setConditions(int conditions) {
        this.conditions = conditions;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Advert setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getMainPhotoUrl() {
        return mainPhotoUrl;
    }

    public Advert setMainPhotoUrl(String mainPhotoUrl) {
        this.mainPhotoUrl = mainPhotoUrl;
        return this;
    }
}
