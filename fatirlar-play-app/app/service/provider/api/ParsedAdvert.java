package service.provider.api;

import java.util.List;

public class ParsedAdvert {

    private long publicationTimestamp;
    private Integer bedrooms;
    private Integer beds;
    private Integer rooms;
    private Integer sq;
    private Integer floor;
    private Integer maxFloor;
    private String address;
    private String description;
    private Double latitude;
    private Double longitude;
    private Integer price;

    private List<String> photos;

    private String userName;
    private long phone;
    private int trustRate;

    private int originId;

    public long getPublicationTimestamp() {
        return publicationTimestamp;
    }

    public ParsedAdvert setPublicationTimestamp(long publicationTimestamp) {
        this.publicationTimestamp = publicationTimestamp;
        return this;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public ParsedAdvert setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
        return this;
    }

    public Integer getBeds() {
        return beds;
    }

    public ParsedAdvert setBeds(Integer beds) {
        this.beds = beds;
        return this;
    }

    public Integer getRooms() {
        return rooms;
    }

    public ParsedAdvert setRooms(Integer rooms) {
        this.rooms = rooms;
        return this;
    }

    public Integer getSq() {
        return sq;
    }

    public ParsedAdvert setSq(Integer sq) {
        this.sq = sq;
        return this;
    }

    public Integer getFloor() {
        return floor;
    }

    public ParsedAdvert setFloor(Integer floor) {
        this.floor = floor;
        return this;
    }

    public Integer getMaxFloor() {
        return maxFloor;
    }

    public ParsedAdvert setMaxFloor(Integer maxFloor) {
        this.maxFloor = maxFloor;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public ParsedAdvert setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ParsedAdvert setDescription(String description) {
        this.description = description;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public ParsedAdvert setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Double getLongitude() {
        return longitude;
    }

    public ParsedAdvert setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public Integer getPrice() {
        return price;
    }

    public ParsedAdvert setPrice(Integer price) {
        this.price = price;
        return this;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public ParsedAdvert setPhotos(List<String> photos) {
        this.photos = photos;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public ParsedAdvert setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public long getPhone() {
        return phone;
    }

    public ParsedAdvert setPhone(long phone) {
        this.phone = phone;
        return this;
    }

    public int getTrustRate() {
        return trustRate;
    }

    public ParsedAdvert setTrustRate(int trustRate) {
        this.trustRate = trustRate;
        return this;
    }

    public int getOriginId() {
        return originId;
    }

    public ParsedAdvert setOriginId(int originId) {
        this.originId = originId;
        return this;
    }

    @Override
    public String toString() {
        return address + "\\" + originId;
    }
}