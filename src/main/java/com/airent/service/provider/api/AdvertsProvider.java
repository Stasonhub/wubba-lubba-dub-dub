package com.airent.service.provider.api;

import java.util.Iterator;

public interface AdvertsProvider {

    String getType();

    Iterator<ParsedAdvertHeader> getHeaders();

    ParsedAdvert getAdvert(ParsedAdvertHeader parsedAdvertHeader);

}