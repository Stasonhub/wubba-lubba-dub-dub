package com.airent.service.provider.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

    public static final String USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36";

    private static Logger logger = LoggerFactory.getLogger(Util.class);

    public static Integer getNumberInsideOf(String val) {
        return Integer.parseInt(getStringNumberInsideOf(val));
    }

    public static Long getLongNumberInsideOf(String val) {
        return Long.parseLong(getStringNumberInsideOf(val));
    }

    private static String getStringNumberInsideOf(String val) {
        StringBuilder result = new StringBuilder();
        boolean collecting = false;
        for (char c : val.toCharArray()) {
            if (c >= '0' && c <= '9') {
                result.append(c);
            } else {
                if (collecting) {
                    break;
                }
                collecting = false;
            }
        }
        if (result.length() == 0) {
            logger.warn("There is no number in: " + val);
            return null;
            //throw new IllegalArgumentException("There is no number in: " + val);
        }
        return result.toString();
    }
}