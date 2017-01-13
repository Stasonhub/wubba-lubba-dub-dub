package com.airent.service.provider;

import com.airent.mapper.AdvertImportMapper;
import com.airent.mapper.AdvertMapper;
import com.airent.mapper.PhotoMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.Photo;
import com.airent.model.User;
import com.airent.service.PhotoService;
import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.ParsedAdvert;
import com.airent.service.provider.api.ParsedAdvertHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdvertImportService {

    private Logger logger = LoggerFactory.getLogger(AdvertImportService.class);

    private List<AdvertsProvider> advertsProviders;

    private AdvertImportMapper advertImportMapper;
    private AdvertMapper advertMapper;
    private PhotoMapper photoMapper;
    private UserMapper userMapper;
    private PhotoService photoService;

    @Autowired
    public AdvertImportService(List<AdvertsProvider> advertsProviders,
                               AdvertImportMapper advertImportMapper,
                               AdvertMapper advertMapper,
                               PhotoMapper photoMapper,
                               UserMapper userMapper,
                               PhotoService photoService) {
        this.advertsProviders = advertsProviders;
        this.advertImportMapper = advertImportMapper;
        this.advertMapper = advertMapper;
        this.photoMapper = photoMapper;
        this.userMapper = userMapper;
        this.photoService = photoService;
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

        Iterator<ParsedAdvertHeader> adverts =
                advertsProvider.getHeaders();
        while (adverts.hasNext()) {
            ParsedAdvertHeader advertHeader = adverts.next();
            if (advertHeader.getPublicationTimestamp() <= (lastImportTime + 60_000)) {
                break;
            }

            ParsedAdvert advert = advertsProvider.getAdvert(advertHeader);
        }
        List<RawAdvert> rawAdverts = Collections.emptyList();//advertsProvider.getAdvertsUntil(lastImportTime);

        for (RawAdvert rawAdvert : rawAdverts) {
            processNewAdvert(rawAdvert);

            // save last import time
            advertImportMapper.saveLastImportTime(advertsProvider.getType(), rawAdvert.getAdvert().getPublicationDate());
        }
    }

    private void processNewAdvert(RawAdvert rawAdvert) {
        Advert matchingAdvert = findMatchingAdvert(rawAdvert);
        User matchingUser = findMatchingUser(rawAdvert);

        if (matchingAdvert != null) {
            /* full duplicate */
            if (matchingUser != null) {
                return;
            }

            // just create new user with half of trust because of duplicate
            User newUser = rawAdvert.getUser();
            newUser.setTrustRate(newUser.getTrustRate() / 2);
            createUser(newUser);
            advertMapper.bindToUser(matchingAdvert.getId(), newUser.getId());
            return;
        }

        /* new advert */
        if (rawAdvert.getPhotos().isEmpty()) {
            throw new IllegalArgumentException("Found advert without photo " + rawAdvert.getAdvert());
        }

        // create new advert in system
        // create everything automatically
        advertMapper.createAdvert(rawAdvert.getAdvert());
        userMapper.createUser(rawAdvert.getUser());
        advertMapper.bindToUser(rawAdvert.getAdvert().getId(), rawAdvert.getUser().getId());

        // move and create photos
        for (Photo photo : rawAdvert.getPhotos()) {
            photo.setAdvertId(rawAdvert.getAdvert().getId());
            photoMapper.createPhoto(photo);
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

    private Advert findMatchingAdvert(RawAdvert rawAdvert) {
        Map<Long, Long> allPhotoHashes = photoMapper.getAllPhotoHashes().stream()
                .collect(Collectors.toMap(Photo::getHash, Photo::getAdvertId, (adv1, adv2) -> {
                    logger.warn("Adverts {} and {} has the same photos", adv1, adv2);
                    return adv1;
                }));
        Optional<Long> anyValue = rawAdvert.getPhotos().stream()
                .map(Photo::getHash)
                .map(hash -> photoService.searchForSame(allPhotoHashes, hash))
                .filter(val -> val != null)
                .findAny();
        if (anyValue.isPresent()) {
            return advertMapper.findById(anyValue.get());
        }
        return null;
    }

    private User findMatchingUser(RawAdvert rawAdvert) {
        User user = rawAdvert.getUser();
        return userMapper.findByPhone(user.getPhone());
    }

}