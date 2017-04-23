package service;

import model.Advert;
import model.User;
import repository.AdvertRepository;
import repository.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService {

    @Inject
    private AdvertRepository advertMapper;

    @Inject
    private UserRepository userMapper;

    public User getUserForAdvert(long advertId) {
        Advert advert = advertMapper.findById(advertId);
        if (advert == null) {
            return null;
        }
        return userMapper.getUserForAdvert(advert.id());
    }

}
