package com.airent.service.provider.avito;

import org.apache.commons.lang3.tuple.Pair;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

public class PhoneParser implements AutoCloseable {

    private Pattern idPattern = Pattern.compile("avito\\.item\\.id[ ]+=[ ]*'([0-9a-z]+)'");
    private Pattern phonePatter = Pattern.compile("avito\\.item\\.phone[ ]+=[ ]*'([0-9a-z]+)'");
    private SecretMixer secretMixer = new SecretMixer();
    private tesseract.TessBaseAPI api;

    @PostConstruct
    public void init() {
        api = new tesseract.TessBaseAPI();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(null, "digits") != 0) {
            throw new IllegalStateException("Couldn't init tesseract");
        }
    }

    public long getPhone(Document advertPage, String advertId) throws IOException {
        Pair<Integer, String> secret = getSecret(advertPage);
        String mixedVal = secretMixer.mix(secret.getLeft(), secret.getRight());

        Connection.Response response = Jsoup
                .connect("https://www.avito.ru/items/phone/" + secret.getLeft() + "?pkey=" + mixedVal)
                .referrer("https://www.avito.ru" + advertId)
                .ignoreContentType(true)
                .execute();
        String body = response.body();
        System.out.println(body);
        return -1;
    }

    private Pair<Integer, String> getSecret(Document document) {
        Elements scriptElements = document.getElementsByTag("script");
        for (Element script : scriptElements) {
            String scriptText = script.html();
            Pair<Integer, String> keys = parseKeysFromScript(scriptText);
            if (keys != null) {
                return keys;
            }
        }
        return null;
    }

    public long parseNumbersFromImage(String dataImagePhoto) {
        String base64Image = dataImagePhoto.split(",")[1];
        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);

        lept.PIX image = lept.pixReadMemPng(imageBytes, imageBytes.length);

        api.SetImage(imageBytes, 100, 100, 1, 5);
        BytePointer outText = api.GetUTF8Text();
        String result = outText.getString();
        System.out.println("OCR output:\n" + outText.getString());
        outText.deallocate();
        pixDestroy(image);

        return Long.parseLong(result);
    }

    public Pair<Integer, String> parseKeysFromScript(String scriptText) {
        Integer id = null;
        String key = null;

        Matcher idMatcher = idPattern.matcher(scriptText);
        if (idMatcher.find()) {
            id = Integer.valueOf(idMatcher.group(1));
        }
        Matcher phoneMatcher = phonePatter.matcher(scriptText);
        if (phoneMatcher.find()) {
            key = phoneMatcher.group(1);
        }
        if (id == null && key == null) {
            return null;
        }
        return Pair.of(id, key);
    }

    @Override
    public void close() throws Exception {
        api.End();
    }

    public class SecretMixer {

        private Pattern pattern = Pattern.compile("[0-9a-f]+");

        private String mix(int number, String key) {
            if (key == null) {
                return "";
            }

            List<String> groups = new ArrayList<>();
            Matcher matcher = pattern.matcher(key);
            while (matcher.find()) {
                groups.add(matcher.group());
            }

            if (number % 2 == 0) {
                Collections.reverse(groups);
            }

            String joinedString = groups.stream().collect(Collectors.joining());

            StringBuilder result = new StringBuilder();
            for (int i = 0; i < joinedString.length(); ++i) {
                if (i % 3 == 0) {
                    result.append(joinedString.charAt(i));
                }
            }
            return result.toString();
        }

    }
}