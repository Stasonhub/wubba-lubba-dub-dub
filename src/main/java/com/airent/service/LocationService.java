package com.airent.service;

import com.airent.model.District;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    public District getdistrictFromAddress(String address) {
        // TODO: complete method to get correct districts (by geoloc, or from streetName)
        return District.CV;
    }


}