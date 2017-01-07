package com.airent.service.provider.totook;

import com.airent.model.Advert;
import com.airent.model.User;
import com.airent.service.LocationService;
import com.airent.service.PhotoService;
import com.airent.service.provider.api.RawAdvert;
import com.airent.service.provider.http.JSoupTorConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Ignore
public class TotookProviderTest {

    @Test
    public void testScanSomething() {
        LocationService locationService = new LocationService();
        locationService.init();

        PhotoService photoService = new PhotoService();

        TotookDateFormatter totookDateFormatter = new TotookDateFormatter();

        try (JSoupTorConnector jSoupTorConnector = new JSoupTorConnector(null)) {
            jSoupTorConnector.start();

            TotookProvider totookProvider = new TotookProvider(jSoupTorConnector, locationService, photoService, totookDateFormatter, 5, "/tmp/photos/2", "", "");

            List<RawAdvert> advertsUntil = totookProvider.getAdvertsUntil(0L);

            advertsUntil.stream().map(RawAdvert::getAdvert).map(Advert::getDescription).forEach(System.out::println);
            advertsUntil.stream().map(RawAdvert::getUser).map(User::getPhone).forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCoordinatesParser() {
        String coordinatesScript = "function fid_totook(ymaps) {\n" +
                "\tvar map = new ymaps.Map(\"ymaps-map-id_totook\", \n" +
                "\t\t{\n" +
                "\t\tcenter: [49.076761, 55.830134], \n" +
                "\t\tzoom: 16, \n" +
                "\t\tcontrols: [\"zoomControl\", \"fullscreenControl\", \"trafficControl\"],\n" +
                "\t\ttype: \"yandex#map\"\n" +
                "\t\t}\n" +
                "\t);\n" +
                "\tmap.controls.add(\"zoomControl\");\n" +
                "\tmyMark = new ymaps.GeoObject({\n" +
                "\t\t// Описание геометрии.\n" +
                "\t\tgeometry: {\n" +
                "\t\t\ttype: \"Point\",\n" +
                "\t\t\tcoordinates: [49.076761, 55.830134]\n" +
                "\t\t},\n" +
                "\t\t// Свойства.\n" +
                "\t\tproperties: {\n" +
                "\t\t}\n" +
                "\t}, {\n" +
                "\t\tpreset: 'twirl#blueIcon',\n" +
                "\t\t//iconLayout: \"islands#blackCircleIcon\", \n" +
                "\t});\t\n" +
                "\tmap.geoObjects.add(myMark);\n" +
                "};";

        LocationService locationService = new LocationService();
        locationService.init();

        PhotoService photoService = new PhotoService();

        TotookDateFormatter totookDateFormatter = new TotookDateFormatter();

        try (JSoupTorConnector jSoupTorConnector = new JSoupTorConnector(null)) {
            jSoupTorConnector.start();

            TotookProvider totookProvider = new TotookProvider(jSoupTorConnector, locationService, photoService, totookDateFormatter, 5, "/tmp/photos/2", "", "");
            Pair<Double, Double> coordinates = totookProvider.getCoordinates(coordinatesScript);

            assertNotNull(coordinates);
            assertNotNull(coordinates.getLeft());
            assertNotNull(coordinates.getRight());

            assertEquals(49.076761, coordinates.getRight(), 0.01);
            assertEquals(55.830134, coordinates.getLeft(), 0.01);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testImageUrl() {
        LocationService locationService = new LocationService();
        locationService.init();

        PhotoService photoService = new PhotoService();

        TotookDateFormatter totookDateFormatter = new TotookDateFormatter();

        try (JSoupTorConnector jSoupTorConnector = new JSoupTorConnector(null)) {
            jSoupTorConnector.start();


            TotookProvider totookProvider = new TotookProvider(jSoupTorConnector, locationService, photoService, totookDateFormatter, 5, "/tmp/photos/2", "", "");
            String imageUrl = totookProvider.getImageUrl("/timthumb.php?src=/upload/iblock/0f7/0_b193_55376477_XXL.jpg&w=134&h=110");
            System.out.println(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}