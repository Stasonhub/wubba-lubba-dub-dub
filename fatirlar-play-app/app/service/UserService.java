package service;

import model.Advert;
import model.User;
import repository.interops.AdvertRepositoryJv;
import repository.interops.UserRepositoryJv;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService {

    @Inject
    private AdvertRepositoryJv advertMapper;

    @Inject
    private UserRepositoryJv userMapper;

    public User getUserForAdvert(int advertId) {
        Advert advert = advertMapper.findById(advertId);
        if (advert == null) {
            return null;
        }
        return userMapper.getUserForAdvert(advert.id());
    }

}
