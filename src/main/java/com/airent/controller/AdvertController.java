package com.airent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AdvertController {

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public String show() {
        return "main";
    }

}
