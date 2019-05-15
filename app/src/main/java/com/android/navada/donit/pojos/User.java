package com.android.navada.donit.pojos;

import java.util.HashMap;

public class User {

    private String name;
    private String email;
    private String mobileNumber;
    private String city;
    private String typeOfUser;
    private HashMap<String, String> orgs;

    public User() {}

    public User(String name, String email, String mobileNumber, String city, String typeOfUser, HashMap<String, String> orgs) {
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.city = city;
        this.typeOfUser = typeOfUser;
        this.orgs = orgs;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getCity() {
        return city;
    }

    public String getTypeOfUser() {
        return typeOfUser;
    }

    public HashMap<String, String> getOrgs() {
        return orgs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setTypeOfUser(String typeOfUser) {
        this.typeOfUser = typeOfUser;
    }

    public void setOrgs(HashMap<String, String> orgs) {
        this.orgs = orgs;
    }
}
