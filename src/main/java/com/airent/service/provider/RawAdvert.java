package com.airent.service.provider;

import com.airent.model.Advert;
import com.airent.model.Photo;
import com.airent.model.User;

import java.util.List;

public class RawAdvert {

    private Advert advert;
    private User user;
    private List<Photo> photos;

    public Advert getAdvert() {
        return advert;
    }

    public void setAdvert(Advert advert) {
        this.advert = advert;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
}