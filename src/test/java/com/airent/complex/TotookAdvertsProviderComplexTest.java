package com.airent.complex;

import com.airent.config.OyoSpringTest;
import com.airent.service.provider.api.ParsedAdvert;
import com.airent.service.provider.api.ParsedAdvertHeader;
import com.airent.service.provider.totook.TotookAdvertsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Iterator;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@OyoSpringTest
@Test(groups = "complex")
public class TotookAdvertsProviderComplexTest extends AbstractTestNGSpringContextTests {

    private Logger logger = LoggerFactory.getLogger(TotookAdvertsProviderComplexTest.class);

    @Autowired
    private TotookAdvertsProvider totookAdvertsProvider;

    @Test//(timeOut = 90_000)
    public void getAdverts() throws Exception {
        assertTrue(totookAdvertsProvider.getMaxItemsToScan() == 20);

        Iterator<ParsedAdvertHeader> headers = totookAdvertsProvider.getHeaders();
        for (int i = 0; i < totookAdvertsProvider.getMaxItemsToScan() && headers.hasNext(); i++) {
            ParsedAdvertHeader header = headers.next();

            ParsedAdvert advert = totookAdvertsProvider.getAdvert(header);
            checkAdvert(advert);
        }
    }

    private void checkAdvert(ParsedAdvert parsedAdvert) {
        assertNotNull(parsedAdvert.getSq());
        assertNotNull(parsedAdvert.getPrice());
        assertNotNull(parsedAdvert.getLatitude());
        assertNotNull(parsedAdvert.getLongitude());

        logger.info("Checked advert {} : {}/{}/{}/{}", parsedAdvert, parsedAdvert.getSq(), parsedAdvert.getPrice(), parsedAdvert.getLatitude(), parsedAdvert.getLongitude());
    }


}