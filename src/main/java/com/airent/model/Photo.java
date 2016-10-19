package com.airent.model;

public class Photo {

    private long id;
    private long advertId;
    private boolean main;
    private String path;

    public long getId() {
        return id;
    }

    public Photo setId(long id) {
        this.id = id;
        return this;
    }

    public long getAdvertId() {
        return advertId;
    }

    public Photo setAdvertId(long advertId) {
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
}
