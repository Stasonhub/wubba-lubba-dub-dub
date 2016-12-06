package com.airent.controller;


import com.airent.mapper.AdvertImportMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AdminController {

    private AdvertImportMapper advertImportMapper;

    @RequestMapping(method = RequestMethod.GET, path = "/admin")
    public void viewImportState() {

    }

    @RequestMapping(method = RequestMethod.POST, path = "/startImport")
    public void startImport(@RequestAttribute("typeKey") String typeKey) {

    }

    private void checkAccess() {

    }


}