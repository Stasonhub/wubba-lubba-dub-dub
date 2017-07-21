package service.provider.avito;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

class AvitoDateFormatter {

    private ZoneId zoneId = ZoneId.of("UTC+03:00");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

    public long getTimestamp(String date) {
        return parseDateTime(toStrictFormat(date)).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * @return time in formatted, zoneId zone
     */
    String toStrictFormat(String date) {
        LocalDate now = LocalDate.now(zoneId);

        String dateFormatted = date.toLowerCase().replace("\u00A0", " ").trim();
        if (dateFormatted.startsWith("сегодня")) {
            int dayOfMonth = now.getDayOfMonth();
            int monthValue = now.getMonthValue();
            return String.format("%s %02d.%02d.%d", splitAndValidate(dateFormatted, 2)[1], dayOfMonth, monthValue,
                    now.getYear());
        } else if (dateFormatted.startsWith("вчера")) {
            int dayOfMonth = now.minusDays(1).getDayOfMonth();
            int monthValue = now.minusDays(1).getMonthValue();
            return String.format("%s %02d.%02d.%d", splitAndValidate(dateFormatted, 2)[1], dayOfMonth, monthValue,
                    now.getYear());
        }

        String[] parts = splitAndValidate(dateFormatted, 3);
        return String.format("%s %02d.%02d.%d", parts[2], Integer.valueOf(parts[0]), replaceMonth(parts[1]), now.getYear());
    }

    private String[] splitAndValidate(String dateFormatted, int count) {
        String[] parts = dateFormatted.split("\\s+");
        if (parts.length != count) {
            throw new IllegalArgumentException("Failed to extract from date: '" + dateFormatted + "'. Part " + count + " actual " + Arrays.toString(parts));
        }
        return parts;
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

    /**
     * Parses formatted dateTime in zoneId zone
     */
    LocalDateTime parseDateTime(String dateTimeText) {
        // TODO: do this elegant
        return LocalDateTime.parse(dateTimeText, dateTimeFormatter).minusHours(3);
    }
}