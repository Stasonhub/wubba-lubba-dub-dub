package util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.function.Function;

public class Utils {

    public static String getStringPart(String description, int maxLength) {
        return (null == description) ? "" : description.substring(0, Math.min(maxLength, description.length())) + "...";
    }

    public static String getRoomsText(int roomsCount) {
        if (roomsCount == 1) {
            return "комната";
        }
        return "комнаты";
    }

    public static boolean checkAtLeastOneCondition(int conditions, int... indexes) {
        for (int i = 0; i < indexes.length; i++) {
            if (checkCondition(conditions, indexes[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkCondition(int conditions, int index) {
        int mask = 1 << index;
        return (conditions & mask) == mask;
    }

    public static <T> Function<WebDriver, T> cv(ExpectedCondition<T> t) {
        return t::apply;
    }


}
