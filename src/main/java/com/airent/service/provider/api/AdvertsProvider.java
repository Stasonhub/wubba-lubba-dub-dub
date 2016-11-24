package com.airent.service.provider.api;

import java.util.List;

public interface AdvertsProvider {

    List<RawAdvert> getAdvertsUntil(long timestamp);

}