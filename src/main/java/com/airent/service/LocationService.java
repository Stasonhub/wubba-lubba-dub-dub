package com.airent.service;

import com.airent.model.District;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import org.apache.commons.io.IOUtils;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private GeometryJSON gjson = new GeometryJSON();
    private Map<District, MultiPolygon> districtGeometryMap;

    @PostConstruct
    public void init() {
        districtGeometryMap = Arrays.stream(District.values()).collect(
                Collectors.toMap(district -> district, district ->
                        getMultiPolygon(loadDistrictGeoData(district))
                ));
    }


    private String loadDistrictGeoData(District district) {
        try {
            ClassPathResource classPathResource = new ClassPathResource("districts/geodata/" + district.name().toLowerCase() + ".json");
            return IOUtils.toString(classPathResource.getInputStream(), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MultiPolygon getMultiPolygon(String districtGeoData) {
        try {
            return gjson.readMultiPolygon(new StringReader(districtGeoData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public District getDistrictFromAddress(double latitude, double longitude) {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        Coordinate coord = new Coordinate(longitude, latitude);
        Point point = geometryFactory.createPoint(coord);

        Optional<District> district = districtGeometryMap
                .entrySet().stream()
                .filter(entry -> entry.getValue().covers(point))
                .findAny().map(Map.Entry::getKey);

        if (!district.isPresent()) {
            System.out.println("Failed to determine district for point " + latitude + "/" + longitude + ". " +
                    "Failback to CV.");
            return District.CV;
        }
        return district.get();
    }


}