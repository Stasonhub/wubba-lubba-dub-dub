package com.airent.service.provider.avito;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PhoneParserTest {

    @Test
    public void parseNumbersFromImage() throws Exception {
        String img = "{\"image64\":\"data:image/png;base64,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\"}\n";
        PhoneParser phoneParser = new PhoneParser(null);
        phoneParser.init();
        assertEquals(9600401225L, phoneParser.parseNumbersFromImage(img));
        phoneParser.close();
    }

    @Test
    public void parseKeysFromScript() throws Exception {
        String script = "var avito = avito || {};\n" +
                " avito.item = avito.item || {};\n" +
                " avito.item.url = '/kazan/kvartiry/1-k_kvartira_36_m_59_et._871555544';\n" +
                " avito.item.id = '871555544';\n" +
                " avito.item.price = '12000';\n" +
                " avito.item.countryHost = 'www.avito.ru';\n" +
                " avito.item.supportPrefix = 'https://support.avito.ru/hc/ru';\n" +
                " avito.item.siteName = 'Avito';\n" +
                " avito.item.isRealty = 1;\n" +
                " avito.item.isMyItem = 0;\n" +
                " avito.item.hasCvPackage = 0;\n" +
                "        avito.item.phone = '6753addcc649sd5702c908sea39c4069406s2a6b088fasb0esd67s1e0576sb940ccedaf57880ac207596069644ce3a0af98056a';  avito.token.update('token[3714225207294]', '75ff730f00773d00');";
        Pair<Integer, String> pair = new PhoneParser(null).parseKeysFromScript(script);
        assertNotNull(pair);
        assertEquals((Integer) 871555544, pair.getLeft());
        assertEquals("6753addcc649sd5702c908sea39c4069406s2a6b088fasb0esd67s1e0576sb940ccedaf57880ac207596069644ce3a0af98056a", pair.getRight());
    }

}