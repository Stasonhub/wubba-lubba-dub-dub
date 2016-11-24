package com.airent.service.provider.api;

import com.airent.model.Advert;
import com.airent.model.User;

public class RawAdvert {

    private Advert advert;
    private User user;

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
}