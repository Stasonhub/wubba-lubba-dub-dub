package com.airent.model.ui;

public class ImportState {

    private String providerName;
    private long lastImportDate;

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public long getLastImportDate() {
        return lastImportDate;
    }

    public void setLastImportDate(long lastImportDate) {
        this.lastImportDate = lastImportDate;
    }
}