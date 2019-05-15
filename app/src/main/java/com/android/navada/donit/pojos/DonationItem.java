package com.android.navada.donit.pojos;

import java.util.HashMap;

public class DonationItem {

    private String description;
    private String donationImageUrl;
    private String deliveryImageUrl;
    private String category;
    private String address;
    private String city;
    private String status;
    private String timeStamp;
    private String donorName;
    private String donorId;
    private String donorContactNumber;
    private String delivereName;
    private String delivererContactNumber;
    private String deliveredDate;
    private String orgLat,orgLng;
    private String deliveryAddress;


    private String chosenOrganizationId="";
    private HashMap<String,Object> donorAddress;

    public DonationItem() {}

    public DonationItem(String description, String donationImageUrl, String deliveryImageUrl, String category, String address, String city, String status, String timeStamp, String donorName, String donorId, String donorContactNumber, String delivereName, String delivererContactNumber, String deliveredDate) {
        this.description = description;
        this.donationImageUrl = donationImageUrl;
        this.deliveryImageUrl = deliveryImageUrl;
        this.category = category;
        this.address = address;
        this.city = city;
        this.status = status;
        this.timeStamp = timeStamp;
        this.donorName = donorName;
        this.donorId = donorId;
        this.donorContactNumber = donorContactNumber;
        this.delivereName = delivereName;
        this.delivererContactNumber = delivererContactNumber;
        this.deliveredDate = deliveredDate;
    }

    public String getDescription() {
        return description;
    }

    public String getDonationImageUrl() {
        return donationImageUrl;
    }

    public String getDeliveryImageUrl() {
        return deliveryImageUrl;
    }

    public String getCategory() {
        return category;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getStatus() {
        return status;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getDonorName() {
        return donorName;
    }

    public String getDonorId() {
        return donorId;
    }

    public String getDonorContactNumber() {
        return donorContactNumber;
    }

    public String getDelivereName() {
        return delivereName;
    }

    public String getDelivererContactNumber() {
        return delivererContactNumber;
    }

    public String getDeliveredDate() {
        return deliveredDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDonationImageUrl(String donationImageUrl) {
        this.donationImageUrl = donationImageUrl;
    }

    public void setDeliveryImageUrl(String deliveryImageUrl) {
        this.deliveryImageUrl = deliveryImageUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public void setDonorContactNumber(String donorContactNumber) {
        this.donorContactNumber = donorContactNumber;
    }

    public void setDelivereName(String delivereName) {
        this.delivereName = delivereName;
    }

    public void setDelivererContactNumber(String delivererContactNumber) {
        this.delivererContactNumber = delivererContactNumber;
    }

    public void setDeliveredDate(String deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public String getChosenOrganizationId() {
        return chosenOrganizationId;
    }

    public void setChosenOrganizationId(String chosenOrganizationId) {
        this.chosenOrganizationId = chosenOrganizationId;
    }

    public HashMap<String, Object> getDonorAddress() {
        return donorAddress;
    }

    public void setDonorAddress(HashMap<String, Object> donorAddress) {
        this.donorAddress = donorAddress;
    }

    public String getOrgLat() {
        return orgLat;
    }

    public void setOrgLat(String orgLat) {
        this.orgLat = orgLat;
    }

    public String getOrgLng() {
        return orgLng;
    }

    public void setOrgLng(String orgLng) {
        this.orgLng = orgLng;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}
