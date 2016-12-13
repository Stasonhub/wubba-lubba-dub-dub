package com.airent.model;

public class Photo {

    private Long id;
    private Long advertId;
    private boolean main;
    private String path;
    private Long hash;

    public Long getId() {
        return id;
    }

    public Photo setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getAdvertId() {
        return advertId;
    }

    public Photo setAdvertId(Long advertId) {
        this.advertId = advertId;
        return this;
    }

    public boolean isMain() {
        return main;
    }

    public Photo setMain(boolean main) {
        this.main = main;
        return this;
    }

    public String getPath() {
        return path;
    }

    public Photo setPath(String path) {
        this.path = path;
        return this;
    }

    public Long getHash() {
        return hash;
    }

    public void setHash(Long hash) {
        this.hash = hash;
    }
}
