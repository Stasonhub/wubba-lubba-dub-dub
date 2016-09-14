package com.airent.controller;

import com.airent.service.AdvertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AdvertController {

    @Autowired
    private AdvertService advertService;

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public String showMainPage(Model model) {
        model.addAttribute("adverts", advertService.getAdvertsForMainPage());
        return "main";
    }

}
