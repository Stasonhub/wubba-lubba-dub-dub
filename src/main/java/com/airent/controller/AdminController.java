package com.airent.controller;


import com.airent.model.Advert;
import com.airent.model.rest.UserInfo;
import com.airent.model.ui.ImportState;
import com.airent.service.AdvertService;
import com.airent.service.LoginService;
import com.airent.service.PhotoService;
import com.airent.service.provider.AdvertImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    private AdvertImportService advertImportService;

    @Autowired
    private AdvertService advertService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private LoginService loginService;

    @RequestMapping(method = RequestMethod.GET, path = "/admin")
    public String viewImportState(Model model) {
        checkAccess();

        List<ImportState> importStates = advertImportService.getProviderTypes().stream().map(v -> {
            ImportState importState = new ImportState();
            importState.setProviderName(v);
            importState.setLastImportDate(advertImportService.getLastImportTime(v));
            return importState;
        }).collect(Collectors.toList());

//        List<Advert> rawAdverts = advertService.getRawAdverts();
//
//        model.addAttribute("importStates", importStates);
//        model.addAttribute("rawAdverts", rawAdverts);
//        model.addAttribute("mainPhotos", photoService.getMainPhotos(rawAdverts));

        return "admin";
    }

    @RequestMapping(method = RequestMethod.POST, path = "/startImport")
    public String startImport() {
        checkAccess();
        advertImportService.runImport();
        return "redirect:admin";
    }

    @RequestMapping(method = RequestMethod.POST, path = "/removeAdvert/{advertId}")
    public String removeAdvert(@PathVariable long advertId) {
        checkAccess();
        advertService.removeAdvert(advertId);
        return "redirect:/admin";
    }

    @RequestMapping(method = RequestMethod.POST, path = "/approveAdvert/{advertId}")
    public String approveAdvert(@PathVariable long advertId) {
        checkAccess();
        //advertService.approveAdvert(advertId);
        return "redirect:/admin";
    }

    private void checkAccess() {
        UserInfo currentUser = loginService.getCurrentUser();
        // TODO: check admin credentials
        if (currentUser == null || currentUser.getPhone() != 9274181281L) {
            throw new IllegalArgumentException("IllegalAccess");
        }
    }


}