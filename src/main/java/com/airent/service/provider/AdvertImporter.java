package com.airent.service.provider;

import com.airent.mapper.AdvertImporterMapper;
import com.airent.model.Advert;
import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.RawAdvert;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdvertImporter {

    private List<AdvertsProvider> advertsProviders;

    private AdvertImporterMapper advertImporterMapper;

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
    }

    private void runImport(AdvertsProvider advertsProvider) {
        long lastImportTime = advertImporterMapper.getLastImportTime(advertsProvider.getType());
        List<RawAdvert> rawAdverts = advertsProvider.getAdvertsUntil(lastImportTime);

        for (RawAdvert rawAdvert : rawAdverts) {
            Advert sameAdvert = findTheSameAdvert(rawAdvert);
            if (sameAdvert != null) {

                continue;
            }
        }
    }

    private Advert findTheSameAdvert(RawAdvert advert) {
        return null;
    }

}