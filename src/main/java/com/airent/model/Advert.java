package com.airent.model;

public class Advert {

    private long id;
    private long userId;
    private long publicationDate;

    private District district;
    private String address;
    private int floor;
    private int maxFloor;
    private int rooms;
    private int sq;
    private int price;
    private boolean withPublicServices;
    private boolean withDeposit;

    private String header;
    private String description;

    private int conditions;
    private int bedrooms;
    private int beds;

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

    public District getDistrict() {
        return district;
    }

    public Advert setDistrict(District district) {
        this.district = district;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Advert setAddress(String address) {
        this.address = address;
        return this;
    }

    public int getFloor() {
        return floor;
    }

    public Advert setFloor(int floor) {
        this.floor = floor;
        return this;
    }

    public int getMaxFloor() {
        return maxFloor;
    }

    public Advert setMaxFloor(int maxFloor) {
        this.maxFloor = maxFloor;
        return this;
    }

    public int getRooms() {
        return rooms;
    }

    public Advert setRooms(int rooms) {
        this.rooms = rooms;
        return this;
    }

    public int getSq() {
        return sq;
    }

    public Advert setSq(int sq) {
        this.sq = sq;
        return this;
    }

    public int getPrice() {
        return price;
    }

    public Advert setPrice(int price) {
        this.price = price;
        return this;
    }

    public boolean isWithPublicServices() {
        return withPublicServices;
    }

    public Advert setWithPublicServices(boolean withPublicServices) {
        this.withPublicServices = withPublicServices;
        return this;
    }

    public boolean isWithDeposit() {
        return withDeposit;
    }

    public Advert setWithDeposit(boolean withDeposit) {
        this.withDeposit = withDeposit;
        return this;
    }

    public String getHeader() {
        return header;
    }

    public Advert setHeader(String header) {
        this.header = header;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Advert setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getConditions() {
        return conditions;
    }

    public Advert setConditions(int conditions) {
        this.conditions = conditions;
        return this;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public Advert setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
        return this;
    }

    public int getBeds() {
        return beds;
    }

    public Advert setBeds(int beds) {
        this.beds = beds;
        return this;
    }
}
