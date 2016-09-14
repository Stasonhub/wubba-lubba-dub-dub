package com.airent.model;

public enum Distinct {
    AV("Авиастроительный"),
    VH("Вахитовский"),
    KR("Кировский"),
    MS("Московский"),
    NS("Ново-Савиновский"),
    PV("Приволжский"),
    CV("Советский");

    private String desc;

    private Distinct(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
