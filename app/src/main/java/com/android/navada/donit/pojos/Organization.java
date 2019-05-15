package com.android.navada.donit.pojos;

import com.android.navada.donit.activities.OrganisationSignUpActivity;

public class Organization {

    private String name;
    private String email;
    private String mobileNumber;
    private String type;
    private String city;
    private String address;
    private String cause;
    private String typeOfUser;
    private boolean approved;
    private Double latitude,longitude;

    public Organization() {

    }

    public Organization(String name, String email, String mobileNumber, String type, String city, String address, String cause, String typeOfUser, boolean approved) {
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.type = type;
        this.city = city;
        this.address = address;
        this.cause = cause;
        this.typeOfUser = typeOfUser;
        this.approved = approved;
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

    public String getType() {
        return type;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getCause() {
        return cause;
    }

    public String getTypeOfUser() {
        return typeOfUser;
    }

    public boolean isApproved() {
        return approved;
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

    public void setType(String type) {
        this.type = type;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public void setTypeOfUser(String typeOfUser) {
        this.typeOfUser = typeOfUser;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }


    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}

