package com.airent.service.provider.avito;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class AvitoDateFormatter {

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

    public long getTimestamp(String date) {
        return parseDateTime(toStrictFormat(date)).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    String toStrictFormat(String date) {
        String dateFormatted = date.toLowerCase().trim();
        if (dateFormatted.startsWith("сегодня")) {
            int dayOfMonth = LocalDate.now().getDayOfMonth();
            int monthValue = LocalDate.now().getMonthValue();
            return String.format("%s %02d.%02d.%d", dateFormatted.split(" ")[1], dayOfMonth, monthValue,
                    LocalDate.now().getYear());
        } else if (dateFormatted.startsWith("вчера")) {
            int dayOfMonth = LocalDate.now().minusDays(1).getDayOfMonth();
            int monthValue = LocalDate.now().minusDays(1).getMonthValue();
            return String.format("%s %02d.%02d.%d", dateFormatted.split(" ")[1], dayOfMonth, monthValue,
                    LocalDate.now().getYear());
        }

        String[] parts = dateFormatted.split(" ");
        return String.format("%s %02d.%02d.%d", parts[2], Integer.valueOf(parts[0]), replaceMonth(parts[1]), LocalDate.now().getYear());
    }

    private int replaceMonth(String monthString) {
        if (monthString.startsWith("янв")) {
            return 1;
        }
        if (monthString.startsWith("фев")) {
            return 2;
        }
        if (monthString.startsWith("март")) {
            return 3;
        }
        if (monthString.startsWith("апр")) {
            return 4;
        }
        if (monthString.startsWith("ма")) {
            return 5;
        }
        if (monthString.startsWith("июн")) {
            return 6;
        }
        if (monthString.startsWith("июл")) {
            return 7;
        }
        if (monthString.startsWith("авг")) {
            return 8;
        }
        if (monthString.startsWith("сент")) {
            return 9;
        }
        if (monthString.startsWith("окт")) {
            return 10;
        }
        if (monthString.startsWith("ноя")) {
            return 11;
        }
        if (monthString.startsWith("дек")) {
            return 12;
        }
        throw new IllegalArgumentException("Unknown month: " + monthString);
    }

    LocalDateTime parseDateTime(String dateTimeText) {
        return LocalDateTime.parse(dateTimeText, dateTimeFormatter);
    }
}