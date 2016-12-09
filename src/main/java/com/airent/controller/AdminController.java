package com.airent.controller;


import com.airent.model.ui.ImportState;
import com.airent.service.AdvertService;
import com.airent.service.provider.AdvertImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestAttribute;
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

    @RequestMapping(method = RequestMethod.GET, path = "/admin")
    public String viewImportState(Model model) {
        List<ImportState> importStates = advertImportService.getProviderTypes().stream().map(v -> {
            ImportState importState = new ImportState();
            importState.setProviderName(v);
            importState.setLastImportDate(advertImportService.getLastImportTime(v));
            return importState;
        }).collect(Collectors.toList());


        model.addAttribute("importStates", importStates);
        model.addAttribute("rawAdverts", advertService.getRawAdverts());

        return "admin";
    }

    @RequestMapping(method = RequestMethod.POST, path = "/startImport")
    public String startImport() {
        advertImportService.runImport();
        return "redirect:admin";
    }

    private void checkAccess() {
    }


}