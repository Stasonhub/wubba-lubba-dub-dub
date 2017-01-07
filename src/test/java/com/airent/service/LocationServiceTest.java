package com.airent.service;

import com.airent.config.OyoSpringTest;
import com.airent.model.District;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@OyoSpringTest
public class LocationServiceTest {

    @Autowired
    private LocationService locationService;

    @Test
    public void getDistrictFromAddress() throws Exception {
        assertEquals(District.AV, locationService.getDistrictFromAddress(55.848331, 49.123076));
        assertEquals(District.CV, locationService.getDistrictFromAddress(55.796723, 49.194938));
        assertEquals(District.KR, locationService.getDistrictFromAddress(55.799807, 49.052511));
        assertEquals(District.MS, locationService.getDistrictFromAddress(55.835249, 49.081940));
        assertEquals(District.NS, locationService.getDistrictFromAddress(55.819754, 49.121420));
        assertEquals(District.PV, locationService.getDistrictFromAddress(55.743842, 49.134096));
        assertEquals(District.VH, locationService.getDistrictFromAddress(55.799821, 49.110070));
    }

}


