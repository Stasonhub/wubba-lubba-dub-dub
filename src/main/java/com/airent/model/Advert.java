package com.airent.model;

public class Advert {

    private long id;
    private long publicationDate;

    private User user;
    private Address address;
    private Distinct distinct;
    private int price;

    private int conditions;
    private String description;

    public long getId() {
        return id;
    }

    public Advert setId(long id) {
        this.id = id;
        return this;
    }

    public long getPublicationDate() {
        return publicationDate;
    }

    public Advert setPublicationDate(long publicationDate) {
        this.publicationDate = publicationDate;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Advert setUser(User user) {
        this.user = user;
        return this;
    }

    public Address getAddress() {
        return address;
    }

    public Advert setAddress(Address address) {
        this.address = address;
        return this;
    }

    public Distinct getDistinct() {
        return distinct;
    }

    public Advert setDistinct(Distinct distinct) {
        this.distinct = distinct;
        return this;
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
}
