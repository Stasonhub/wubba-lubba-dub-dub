package com.airent.model;

public class User {

    private long id;
    private long phone;
    private String name;
    private String password;
    private int trustRate;
    private boolean registered;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTrustRate() {
        return trustRate;
    }

    public void setTrustRate(int trustRate) {
        this.trustRate = trustRate;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isRegistered() {
        return registered;
    }

}
