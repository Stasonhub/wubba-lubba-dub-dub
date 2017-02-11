package com.airent.parsers.totook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class TotookDateFormatter {

    private Logger logger = LoggerFactory.getLogger(TotookDateFormatter.class);

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

    public long getTimestamp(String date) {
        return parseDateTime(toStrictFormat(date)).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    String toStrictFormat(String date) {
        logger.info("Converting date {}", date);

        String dateInLowerCase = date.toLowerCase().trim();
        if (dateInLowerCase.startsWith("сегодня")) {
            int dayOfMonth = LocalDate.now().getDayOfMonth();
            int monthValue = LocalDate.now().getMonthValue();
            return String.format("%s %02d.%02d.%d", dateInLowerCase.split(" ")[2], dayOfMonth, monthValue,
                    LocalDate.now().getYear());
        } else if (dateInLowerCase.startsWith("вчера")) {
            int dayOfMonth = LocalDate.now().minusDays(1).getDayOfMonth();
            int monthValue = LocalDate.now().minusDays(1).getMonthValue();
            return String.format("%s %02d.%02d.%d", dateInLowerCase.split(" ")[2], dayOfMonth, monthValue,
                    LocalDate.now().getYear());
        }

        return String.format("00:00 %s", dateInLowerCase);
    }


    LocalDateTime parseDateTime(String dateTimeText) {
        return LocalDateTime.parse(dateTimeText, dateTimeFormatter);
    }
}