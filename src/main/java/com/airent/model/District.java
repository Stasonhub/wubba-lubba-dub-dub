package com.airent.model;

public enum District {
    AV("Авиастроительный"),
    CV("Советский"),
    KR("Кировский"),
    MS("Московский"),
    NS("Ново-Савиновский"),
    PV("Приволжский"),
    VH("Вахитовский");

    private String desc;

    private District(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
