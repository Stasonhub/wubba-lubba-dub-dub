package com.airent.service.provider;

import com.airent.mapper.AdvertImportMapper;
import com.airent.mapper.AdvertMapper;
import com.airent.mapper.PhotoMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.Photo;
import com.airent.model.User;
import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.RawAdvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvertImportService {

    private List<AdvertsProvider> advertsProviders;

    private AdvertImportMapper advertImportMapper;
    private AdvertMapper advertMapper;
    private PhotoMapper photoMapper;
    private UserMapper userMapper;

    @Autowired
    public AdvertImportService(List<AdvertsProvider> advertsProviders, AdvertImportMapper advertImportMapper, AdvertMapper advertMapper, PhotoMapper photoMapper, UserMapper userMapper) {
        this.advertsProviders = advertsProviders;
        this.advertImportMapper = advertImportMapper;
        this.advertMapper = advertMapper;
        this.photoMapper = photoMapper;
        this.userMapper = userMapper;
    }

    public void runImport(String type) {
        Objects.requireNonNull(type);
        Optional<AdvertsProvider> advertsProvider =
                advertsProviders.stream().filter(advertProvider -> advertProvider.getType().equals(type)).findFirst();
        if (!advertsProvider.isPresent()) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }

        runImport(advertsProvider.get());
    }

    public void runImport() {
        for (AdvertsProvider advertsProvider : advertsProviders) {
            runImport(advertsProvider);
        }
    }

    private void runImport(AdvertsProvider advertsProvider) {
        long lastImportTime = advertImportMapper.getLastImportTime(advertsProvider.getType());
        List<RawAdvert> rawAdverts = advertsProvider.getAdvertsUntil(lastImportTime);

        for (RawAdvert rawAdvert : rawAdverts) {
            Advert sameAdvert = findTheSameAdvert(rawAdvert);
            if (sameAdvert != null) {
                User newUser = rawAdvert.getUser();
                int newUserTrustRate = newUser.getTrustRate();

                User currentUser = userMapper.getUserForAdvert(sameAdvert.getId());
                int currentUserTrustRate = currentUser.getTrustRate();

                if (newUserTrustRate > currentUserTrustRate) {
                    // rebind to new user and remove half of trust on existing
                    currentUser.setTrustRate(currentUserTrustRate / 2);
                    userMapper.updateUser(currentUser);
                    createUser(newUser);
                    advertMapper.bindToMainUser(sameAdvert.getId(), newUser.getId());
                    continue;
                }

                // just create new user with half of trust initiated by provider
                newUser.setTrustRate(newUserTrustRate / 2);
                createUser(newUser);
                advertMapper.bindToUser(sameAdvert.getId(), newUser.getId());
                continue;
            }

            if (rawAdvert.getPhotos().isEmpty()) {
                throw new IllegalArgumentException("Found advert without photo " + rawAdvert.getAdvert());
            }

            // create new advert in system
            advertMapper.createAdvert(rawAdvert.getAdvert());
            userMapper.createUser(rawAdvert.getUser());
            advertMapper.bindToMainUser(rawAdvert.getAdvert().getId(), rawAdvert.getUser().getId());

            // move and create photos
            for (Photo photo : rawAdvert.getPhotos()) {
                photo.setAdvertId(rawAdvert.getAdvert().getId());
                photoMapper.createPhoto(photo);
            }

            // save last import time
            advertImportMapper
                    .saveLastImportTime(advertsProvider.getType(), rawAdvert.getAdvert().getPublicationDate());
        }
    }

    private void createUser(User newUser) {
        if (newUser.getId() == null) {
            if (newUser.isRegistered()) {
                throw new IllegalStateException("New user marked as registered");
            }
            userMapper.createUser(newUser);
        }
    }

    public List<String> getProviderTypes() {
        return advertsProviders.stream().map(AdvertsProvider::getType).collect(Collectors.toList());
    }

    public long getLastImportTime(String typeName) {
        return advertImportMapper.getLastImportTime(typeName);
    }

    private Advert findTheSameAdvert(RawAdvert advert) {
        return null;
    }


}