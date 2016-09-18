package com.airent.model;

public enum District {
    AV("Авиастроительный"),
    VH("Вахитовский"),
    KR("Кировский"),
    MS("Московский"),
    NS("Ново-Савиновский"),
    PV("Приволжский"),
    CV("Советский");

    private String desc;

    private District(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
