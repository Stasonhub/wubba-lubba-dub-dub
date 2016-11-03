package com.airent.util;

import java.util.Objects;

public class PhoneNumber {

    /**
     * '+7 (927) 400-0007'
     *
     * @param number
     * @return
     * @throws NumberFormatException, NullPointerException
     */
    public static long normalize(String number) {
        Objects.requireNonNull(number);

        if (number.length() != 17
                || number.charAt(0) != '+'
                || number.charAt(1) != '7'
                || number.charAt(2) != ' '
                || number.charAt(3) != '('
                || number.charAt(7) != ')'
                || number.charAt(8) != ' '
                || number.charAt(12) != '-') {
            throw new NumberFormatException();
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(number.substring(4, 7));
        stringBuilder.append(number.substring(9, 12));
        stringBuilder.append(number.substring(13, 17));
        return Long.valueOf(stringBuilder.toString());
    }
}
