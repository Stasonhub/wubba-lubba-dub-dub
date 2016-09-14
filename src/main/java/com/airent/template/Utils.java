package com.airent.template;

public class Utils {

    public static String getRoomsText(int roomsCount) {
        if (roomsCount == 1) {
            return "комната";
        }
        return "комнаты";
    }

}
