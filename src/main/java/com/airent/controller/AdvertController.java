package com.airent.controller;

import com.airent.model.Advert;
import com.airent.model.District;
import com.airent.model.Photo;
import com.airent.model.rest.SearchRequest;
import com.airent.model.ui.AdvertPrices;
import com.airent.model.ui.SearchBoxState;
import com.airent.service.AdvertService;
import com.airent.service.LoginService;
import com.airent.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdvertController {

    @Autowired
    private AdvertService advertService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private LoginService loginService;

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public String showMainPage(Model model) {
        List<Advert> adverts = advertService.getAdvertsForMainPage();
        model.addAttribute("adverts", adverts);
        model.addAttribute("mainPhotos", photoService.getMainPhotos(adverts));
        model.addAttribute("currentUser", loginService.getCurrentUser());
        return "main";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/search")
    public String searchAdverts(SearchRequest searchRequest, Model model) {
        SearchRequest normalized = normalize(searchRequest);

        List<Advert> adverts = advertService.getAdverts(normalized);
        int pagesCount = advertService.getPagesCount(normalized);
        model.addAttribute("adverts", adverts);
        model.addAttribute("mainPhotos", photoService.getMainPhotos(adverts));
        model.addAttribute("searchRequest", normalized);
        model.addAttribute("pagesCount", pagesCount);
        model.addAttribute("currentUser", loginService.getCurrentUser());
        return "search";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/advert/{advertId}")
    public String getAdvertDetails(@PathVariable long advertId, Model model) {
        Advert advert = advertService.getAdvert(advertId);
        if (advert == null) {
            throw new IllegalArgumentException("Объявление не найдено");
        }

        List<Photo> photos = photoService.getPhotos(advert);

        model.addAttribute("advert", advert);
        model.addAttribute("mainPhoto", photos.stream().filter(Photo::isMain).findFirst().get());
        model.addAttribute("otherPhotos", photos.stream().filter(v -> !v.isMain()).collect(Collectors.toList()));
        model.addAttribute("currentUser", loginService.getCurrentUser());
        return "advert-detail";
    }

    private SearchRequest normalize(SearchRequest searchRequest) {
        SearchRequest normalized = new SearchRequest();

        normalized.setDistricts(searchRequest.getDistricts() == null ? Collections.emptyList() : searchRequest.getDistricts());

        if (!searchRequest.isRoom1() && !searchRequest.isRoom2() && !searchRequest.isRoom3()) {
            normalized.setRoom1(true);
        } else {
            normalized.setRoom1(searchRequest.isRoom1());
            normalized.setRoom2(searchRequest.isRoom2());
            normalized.setRoom3(searchRequest.isRoom3());
        }

        List<Integer> priceRange = searchRequest.getPriceRange();
        if (priceRange != null && priceRange.size() == 2 && priceRange.get(0) > 0 && priceRange.get(1) > 0 && priceRange.get(1) > priceRange.get(0)) {
            normalized.setPriceRange(priceRange);
        } else {
            List<Integer> defaultPriceRange = new ArrayList<>();
            defaultPriceRange.add(8);
            defaultPriceRange.add(60);
            normalized.setPriceRange(defaultPriceRange);
        }

        normalized.setPage(searchRequest.getPage());

        return normalized;
    }

    private SearchBoxState getSearchBoxDefaultState(AdvertPrices advertPrices) {
        SearchBoxState searchBoxState = new SearchBoxState();
        searchBoxState.setAvSelected(true);
        searchBoxState.setVhSelected(true);
        searchBoxState.setKrSelected(true);
        searchBoxState.setMsSelected(true);
        searchBoxState.setNsSelected(true);
        searchBoxState.setPvSelected(true);
        searchBoxState.setCvSelected(true);
        if (advertPrices != null) {
            searchBoxState.setPriceMin(advertPrices.getPriceMin() / 1000);
            searchBoxState.setPriceMax(advertPrices.getPriceMax() / 1000);
        } else {
            searchBoxState.setPriceMin(1);
            searchBoxState.setPriceMax(120);
        }
        searchBoxState.setPriceFrom(searchBoxState.getPriceMin());
        searchBoxState.setPriceTo(searchBoxState.getPriceMax());
        return searchBoxState;
    }

    private SearchBoxState getSearchBoxState(SearchRequest searchRequest, AdvertPrices advertPrices) {
        SearchBoxState searchBoxState = new SearchBoxState();
        searchBoxState.setAvSelected(searchRequest.getDistricts().contains(District.AV));
        searchBoxState.setVhSelected(searchRequest.getDistricts().contains(District.VH));
        searchBoxState.setKrSelected(searchRequest.getDistricts().contains(District.KR));
        searchBoxState.setMsSelected(searchRequest.getDistricts().contains(District.MS));
        searchBoxState.setNsSelected(searchRequest.getDistricts().contains(District.NS));
        searchBoxState.setPvSelected(searchRequest.getDistricts().contains(District.PV));
        searchBoxState.setCvSelected(searchRequest.getDistricts().contains(District.CV));
        searchBoxState.setRooms1Pressed(searchRequest.isRoom1());
        searchBoxState.setRooms2Pressed(searchRequest.isRoom2());
        searchBoxState.setRooms3Pressed(searchRequest.isRoom3());
        searchBoxState.setPriceMin(advertPrices.getPriceMin() / 1000);
        searchBoxState.setPriceMax(advertPrices.getPriceMax() / 1000);
        searchBoxState.setPriceFrom(Math.max(searchBoxState.getPriceMin(), searchRequest.getPriceRange().get(0)));
        searchBoxState.setPriceTo(Math.min(searchBoxState.getPriceMax(), searchRequest.getPriceRange().get(1)));
        return searchBoxState;
    }

}
