package com.airent.controller;

import com.airent.model.AdvertPrices;
import com.airent.model.District;
import com.airent.model.rest.SearchRequest;
import com.airent.model.ui.SearchBoxState;
import com.airent.service.AdvertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdvertController {

    @Autowired
    private AdvertService advertService;

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public String showMainPage(Model model) {
        model.addAttribute("adverts", advertService.getAdvertsForMainPage());
        model.addAttribute("sb", getSearchBoxDefaultState(advertService.getAdvertPrices()));
        return "main";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/loadMore")
    public String loadMoreAdverts(@RequestParam long timestampUntil, Model model) {
        model.addAttribute("adverts", advertService.getAdvertsForMainPageFrom(timestampUntil));
        return "fragments/advert :: advertsForm";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/search")
    public String searchAdverts(SearchRequest searchRequest, Model model) {
        model.addAttribute("adverts", advertService.searchAdvertsUntilTime(searchRequest, System.currentTimeMillis()));
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("sb", getSearchBoxState(searchRequest, advertService.getAdvertPrices()));
        return "search";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/search/loadMore")
    public String searchLoadMoreAdverts(SearchRequest searchRequest, @RequestParam long timestampUntil, Model model) {
        model.addAttribute("adverts", advertService.searchAdvertsUntilTime(searchRequest, timestampUntil));
        return "fragments/advert :: advertsForm";
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
        searchBoxState.setPriceMin(advertPrices.getPriceMin() / 1000);
        searchBoxState.setPriceMax(advertPrices.getPriceMax() / 1000);
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
        searchBoxState.setRooms1Pressed(searchRequest.isRooms1());
        searchBoxState.setRooms2Pressed(searchRequest.isRooms2());
        searchBoxState.setRooms3Pressed(searchRequest.isRooms3());
        searchBoxState.setPriceFrom(searchRequest.getPriceRange().get(0));
        searchBoxState.setPriceTo(searchRequest.getPriceRange().get(1));
        searchBoxState.setPriceMin(advertPrices.getPriceMin() / 1000);
        searchBoxState.setPriceMax(advertPrices.getPriceMax() / 1000);
        return searchBoxState;
    }

}
