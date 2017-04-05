package com.airent.controller;

import com.airent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.POST, path = "/user")
    public String getUserForAdvert(long advertId, Model model) {
        model.addAttribute("user", userService.getUserForAdvert(advertId));
        return "fragment/user-info :: userInfo";
    }

}
