package com.airent.service.provider;

import com.airent.mapper.AdvertImportMapper;
import com.airent.mapper.AdvertMapper;
import com.airent.mapper.PhotoMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.Photo;
import com.airent.model.User;
import com.airent.service.LocationService;
import com.airent.service.PhotoService;
import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.ParsedAdvert;
import com.airent.service.provider.api.ParsedAdvertHeader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class AdvertImportService {

    private Logger logger = LoggerFactory.getLogger(AdvertImportService.class);

    private List<AdvertsProvider> advertsProviders;

    private LocationService locationService;
    private AdvertImportMapper advertImportMapper;
    private AdvertMapper advertMapper;
    private PhotoMapper photoMapper;
    private UserMapper userMapper;
    private PhotoService photoService;
    private PhotoContentService photoContentService;

    @Autowired
    public AdvertImportService(List<AdvertsProvider> advertsProviders,
                               LocationService locationService,
                               AdvertImportMapper advertImportMapper,
                               AdvertMapper advertMapper,
                               PhotoMapper photoMapper,
                               UserMapper userMapper,
                               PhotoService photoService,
                               PhotoContentService photoContentService) {
        this.advertsProviders = advertsProviders;
        this.locationService = locationService;
        this.advertImportMapper = advertImportMapper;
        this.advertMapper = advertMapper;
        this.photoMapper = photoMapper;
        this.userMapper = userMapper;
        this.photoService = photoService;
        this.photoContentService = photoContentService;
    }

    public List<String> getProviderTypes() {
        return advertsProviders.stream().map(AdvertsProvider::getType).collect(Collectors.toList());
    }

    public long getLastImportTime(String typeName) {
        return advertImportMapper.getLastImportTime(typeName);
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
        // run for importers
        advertsProviders.stream().filter(v -> !v.isVerifier()).forEach(this::runImport);
        // run for verifiers
        advertsProviders.stream().filter(AdvertsProvider::isVerifier).forEach(this::runImport);
    }

    private void runImport(AdvertsProvider advertsProvider) {
        logger.info("Started import {}", advertsProvider.getType());

        long lastImportTime = advertImportMapper.getLastImportTime(advertsProvider.getType());

        Long firstAdvertTs = null;
        Iterator<ParsedAdvertHeader> adverts = advertsProvider.getHeaders();
        logger.info("Got headers for type {}", advertsProvider.getType());
        int maxItemsToScan = advertsProvider.getMaxItemsToScan();
        for (int i = 0; i < maxItemsToScan && adverts.hasNext(); ) {
            ParsedAdvertHeader advertHeader = adverts.next();
            if (advertHeader.getPublicationTimestamp() <= lastImportTime) {
                logger.info("Stopping scan. Last import ts={}, advert publication is {}. Advert {}",
                        lastImportTime, advertHeader.getPublicationTimestamp(), advertHeader.getAdvertUrl());
                break;
            }
            if (firstAdvertTs == null) {
                firstAdvertTs = advertHeader.getPublicationTimestamp();
            }
            try {
                ParsedAdvert advert = advertsProvider.getAdvert(advertHeader);
                logger.info("Checking/persisting advert {} for type {}", advert.getPublicationTimestamp(), advertsProvider.getType());
                if (advertsProvider.isVerifier()) {
                    verifyAdvert(advert);
                } else {
                    if (checkAdvert(advert)) {
                        persistAdvert(advertsProvider, advert);
                    } else {
                        logger.info("Advert {} is not correct, ignored", advert.getPublicationTimestamp(), advertsProvider.getType());
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to process advert {}", advertHeader, e);
            }

            i++;
        }


        // save first advert import time (latest by value)
        if (firstAdvertTs != null) {
            logger.info("Saving import time {} for {}", firstAdvertTs, advertsProvider.getType());
            advertImportMapper.saveLastImportTime(advertsProvider.getType(), firstAdvertTs);
        }
    }

    private boolean checkAdvert(ParsedAdvert parsedAdvert) {
        if (checkAndWarn(() -> StringUtils.isEmpty(parsedAdvert.getAddress()),
                () -> logger.warn("Address is empty for advert {}", parsedAdvert))) {
            return false;
        } else if (checkAndWarn(() -> parsedAdvert.getRooms() == null,
                () -> logger.warn("Rooms is null for advert {}", parsedAdvert))) {
            return false;
        } else if (checkAndWarn(() -> parsedAdvert.getPrice() == null,
                () -> logger.warn("Price is empty for advert {}", parsedAdvert))) {
            return false;
        } else if (checkAndWarn(() -> parsedAdvert.getPhotos() == null || parsedAdvert.getPhotos().isEmpty(),
                () -> logger.warn("Photos is empty for advert {}", parsedAdvert))) {
            return false;
        }
        return true;
    }

    private void verifyAdvert(ParsedAdvert parsedAdvert) throws IOException {
        Advert matchingAdvert = advertMapper.findBySqPriceCoords(parsedAdvert.getSq(), parsedAdvert.getPrice(), parsedAdvert.getLatitude(), parsedAdvert.getLongitude());
        if (matchingAdvert != null) {
            // find match by advert/partial user phone
            List<User> matchingUsers = userMapper.findByStartingFourNumbers(parsedAdvert.getPhone());
            if (matchingUsers.isEmpty()) {
                logger.warn("Verification. Failed to find user for advert {} with number {}", parsedAdvert, parsedAdvert.getPhone());
                return;
            }

            if (matchingUsers.size() > 1) {
                logger.warn("Verification. Found more than one matching users for advert {}. Number {}. Users {}", parsedAdvert, parsedAdvert.getPhone(),
                        matchingUsers.stream().map(User::getId).collect(Collectors.toList()));
                return;
            }

            // set current user new rate
            // set other users /4 rate
            userMapper.arrangeRate(matchingAdvert.getId(), matchingUsers.get(0).getId(), parsedAdvert.getTrustRate(), 0.25);
        } else {
            logger.warn("Verification. Failed to find advert {}", parsedAdvert);
        }

    }

    private boolean persistAdvert(AdvertsProvider advertsProvider, ParsedAdvert parsedAdvert) throws IOException {
        List<Photo> photos = photoContentService.savePhotos(advertsProvider.getType(), parsedAdvert);

        Advert matchingAdvert = findMatchingAdvertByPhotos(photos);
        User matchingUser = userMapper.findByPhone(parsedAdvert.getPhone());

        if (matchingAdvert != null) {
            /* full duplicate */
            if (matchingUser != null) {
                return false;
            }

            // found new user for the same advert
            // remove half of trust
            User user = new User();
            user.setRegistered(false);
            user.setTrustRate(parsedAdvert.getTrustRate() / 2);
            user.setPhone(parsedAdvert.getPhone());
            user.setName(parsedAdvert.getUserName());
            userMapper.createUser(user);
            advertMapper.bindToUser(matchingAdvert.getId(), user.getId());
            return false;
        }

        Advert advert = new Advert();
        advert.setPublicationDate(parsedAdvert.getPublicationTimestamp());
        advert.setBedrooms(parsedAdvert.getBedrooms());
        advert.setBeds(parsedAdvert.getBeds());
        advert.setRooms(parsedAdvert.getRooms());
        advert.setSq(parsedAdvert.getSq());
        advert.setFloor(parsedAdvert.getFloor());
        advert.setMaxFloor(parsedAdvert.getMaxFloor());
        advert.setAddress(parsedAdvert.getAddress());
        advert.setDescription(parsedAdvert.getDescription());
        advert.setLatitude(parsedAdvert.getLatitude());
        advert.setLongitude(parsedAdvert.getLongitude());
        advert.setPrice(parsedAdvert.getPrice());
        advert.setDistrict(locationService.getDistrictFromAddress(advert.getLatitude(), advert.getLongitude()));
        advertMapper.createAdvert(advert);

        if (matchingUser != null) {
            // found another one advert from the same user
            // remove 4x trust
            matchingUser.setTrustRate(matchingUser.getTrustRate() / 4);
            userMapper.updateUser(matchingUser);
            advertMapper.bindToUser(advert.getId(), matchingUser.getId());
        } else {
            // just create new user and bind advert
            User user = new User();
            user.setRegistered(false);
            user.setTrustRate(parsedAdvert.getTrustRate());
            user.setPhone(parsedAdvert.getPhone());
            user.setName(parsedAdvert.getUserName());
            userMapper.createUser(user);
            advertMapper.bindToUser(advert.getId(), user.getId());
        }

        // persist photos
        for (Photo photo : photos) {
            photo.setAdvertId(advert.getId());
            photoMapper.createPhoto(photo);
        }

        return true;
    }

    private Advert findMatchingAdvertByPhotos(List<Photo> photos) {
        Map<Long, Long> allPhotoHashes = photoMapper.getAllPhotoHashes().stream()
                .collect(Collectors.toMap(Photo::getHash, Photo::getAdvertId, (adv1, adv2) -> {
                    logger.warn("Adverts {} and {} has the same photos", adv1, adv2);
                    return adv1;
                }));
        Optional<Long> anyValue = photos.stream()
                .map(Photo::getHash)
                .map(hash -> photoService.searchForSame(allPhotoHashes, hash))
                .filter(val -> val != null)
                .findAny();
        if (anyValue.isPresent()) {
            return advertMapper.findById(anyValue.get());
        }
        return null;
    }

    private boolean checkAndWarn(Supplier<Boolean> checker, Runnable warner) {
        boolean value = checker.get();
        if (value) {
            warner.run();
        }
        return value;
    }

}