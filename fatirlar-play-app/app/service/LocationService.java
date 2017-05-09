package service;

import model.District;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import org.apache.commons.io.IOUtils;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import play.api.Play;

import javax.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class LocationService {

    private GeometryJSON gjson = new GeometryJSON();
    private volatile Map<District, MultiPolygon> districtGeometryMap;

    private void init() {
        if (districtGeometryMap == null) {
            synchronized (this) {
                if (districtGeometryMap == null) {
                    districtGeometryMap = Arrays.stream(District.values()).collect(
                            Collectors.toMap(district -> district, district ->
                                    getMultiPolygon(loadDistrictGeoData(district))
                            ));
                }
            }
        }
    }


    private String loadDistrictGeoData(District district) {
        try {
            InputStream districtGeoData = Play.current().classloader().getResourceAsStream("districts/geodata/" + district.name().toLowerCase() + ".json");
            return IOUtils.toString(districtGeoData, "UTF-8");
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
        init();

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