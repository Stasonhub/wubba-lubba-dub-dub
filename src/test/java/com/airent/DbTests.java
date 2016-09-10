package com.airent;


import com.airent.mapper.AdvertMapper;
import com.airent.model.Advert;
import com.airent.model.Distinct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DbTests {

    @Autowired
    private AdvertMapper advertMapper;

    @Test
    public void testInsertSelectAdvert() {
        Advert advert = new Advert();
        advert.setPublicationDate(2L);
        advert.setConditions(2);
        advert.setDescription("Advert");
        advert.setDistrict(Distinct.KR);
        advert.setPrice(42);

        advertMapper.insertAdvert(advert);

        Advert selectedAdvert = advertMapper.findById(advert.getId());

        assertNotNull(selectedAdvert);
        assertEquals(advert.getPublicationDate(), selectedAdvert.getPublicationDate());
        assertEquals(advert.getConditions(), selectedAdvert.getConditions());
        assertEquals(advert.getDescription(), selectedAdvert.getDescription());
        assertEquals(advert.getDistrict(), selectedAdvert.getDistrict());
        assertEquals(advert.getPrice(), selectedAdvert.getPrice());
    }

}
