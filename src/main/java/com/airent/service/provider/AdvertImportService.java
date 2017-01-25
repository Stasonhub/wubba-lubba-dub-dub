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
        for (AdvertsProvider advertsProvider : advertsProviders) {
            runImport(advertsProvider);
        }
    }

    private void runImport(AdvertsProvider advertsProvider) {
        long lastImportTime = advertImportMapper.getLastImportTime(advertsProvider.getType());

        Long firstAdvertTs = null;
        Iterator<ParsedAdvertHeader> adverts = advertsProvider.getHeaders();
        while (adverts.hasNext()) {
            ParsedAdvertHeader advertHeader = adverts.next();
            if (advertHeader.getPublicationTimestamp() <= (lastImportTime + 60_000)) {
                break;
            }
            if (firstAdvertTs == null) {
                firstAdvertTs = advertHeader.getPublicationTimestamp();
            }
            try {
                ParsedAdvert advert = advertsProvider.getAdvert(advertHeader);
                if (checkAdvert(advert)) {
                    persistAdvert(advertsProvider, advert);
                }
            } catch (Exception e) {
                logger.warn("Failed to process advert {}", advertHeader, e);
            }
        }


        // save first advert import time (latest by value)
        if (firstAdvertTs != null) {
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
        } else if (checkAndWarn(() -> parsedAdvert.getPhotos().isEmpty(),
                () -> logger.warn("Photos is empty for advert {}", parsedAdvert))) {
            return false;
        }
        return true;
    }

    private void persistAdvert(AdvertsProvider advertsProvider, ParsedAdvert parsedAdvert) throws IOException {
        List<Photo> photos = photoContentService.savePhotos(advertsProvider.getType(), parsedAdvert);

        Advert matchingAdvert = findMatchingAdvert(photos);
        User matchingUser = findMatchingUser(parsedAdvert);

        if (matchingAdvert != null) {
            /* full duplicate */
            if (matchingUser != null) {
                return;
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
            return;
        }

        Advert advert = new Advert();
        advert.setPublicationDate(parsedAdvert.getPublicationTimestamp());
        advert.setBedrooms(parsedAdvert.getBedrooms());
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
        }

        // persist photos
        for (Photo photo : photos) {
            photoMapper.createPhoto(photo);
        }
    }

    private Advert findMatchingAdvert(List<Photo> photos) {
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

    private User findMatchingUser(ParsedAdvert parsedAdvert) {
        return userMapper.findByPhone(parsedAdvert.getPhone());
    }

    private boolean checkAndWarn(Supplier<Boolean> checker, Runnable warner) {
        boolean value = checker.get();
        if (value) {
            warner.run();
        }
        return value;
    }

}