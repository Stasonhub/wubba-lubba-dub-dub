package com.airent.service.provider.api;

import java.util.List;

public interface AdvertsProvider {

    String getType();

    List<RawAdvert> getAdvertsUntil(long timestamp);

}