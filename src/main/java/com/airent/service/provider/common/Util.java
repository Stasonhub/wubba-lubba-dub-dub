package com.airent.service.provider.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

    private static Logger logger = LoggerFactory.getLogger(Util.class);

    public static Integer getNumberInsideOf(String val) {
        String stringNumberInsideOf = getStringNumberInsideOf(val);
        return stringNumberInsideOf == null ? null : Integer.parseInt(stringNumberInsideOf);
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