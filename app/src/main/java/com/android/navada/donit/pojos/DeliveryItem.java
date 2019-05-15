package com.android.navada.donit.pojos;

public class DeliveryItem {

    private String status;
    private String timeStamp;
    private String donationId;
    private String donorName;
    private String donorContactNumber;
    private String sourceAddress;
    private String destinationAddess;
    private String donationImageURL;
    private String deliveryImageUrl;
    private String delivererId;
    private String delivererName;
    private String orgName;
    private String orgLat,orgLng;
    private String donorLat,donorLng;

    public DeliveryItem() {}

    public DeliveryItem(String status, String timeStamp, String donationId, String donorName, String donorContactNumber, String sourceAddress, String destinationAddess, String donationImageURL, String deliveryImageURL, String delivererId, String delivererName, String orgName) {
        this.status = status;
        this.timeStamp = timeStamp;
        this.donationId = donationId;
        this.donorName = donorName;
        this.donorContactNumber = donorContactNumber;
        this.sourceAddress = sourceAddress;
        this.destinationAddess = destinationAddess;
        this.donationImageURL = donationImageURL;
        this.deliveryImageUrl = deliveryImageURL;
        this.delivererId = delivererId;
        this.delivererName = delivererName;
        this.orgName = orgName;
    }

    public String getStatus() {
        return status;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getDonationId() {
        return donationId;
    }

    public String getDonorName() {
        return donorName;
    }

    public String getDonorContactNumber() {
        return donorContactNumber;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public String getDestinationAddess() {
        return destinationAddess;
    }

    public String getDonationImageURL() {
        return donationImageURL;
    }

    public String getDeliveryImageURL() {
        return deliveryImageUrl;
    }

    public String getDelivererId() {
        return delivererId;
    }

    public String getDelivererName() {
        return delivererName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public void setDonorContactNumber(String donorContactNumber) {
        this.donorContactNumber = donorContactNumber;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public void setDestinationAddess(String destinationAddess) {
        this.destinationAddess = destinationAddess;
    }

    public void setDonationImageURL(String donationImageURL) {
        this.donationImageURL = donationImageURL;
    }

    public void setDeliveryImageURL(String deliveryImageURL) {
        this.deliveryImageUrl = deliveryImageURL;
    }

    public void setDelivererId(String delivererId) {
        this.delivererId = delivererId;
    }

    public void setDelivererName(String delivererName) {
        this.delivererName = delivererName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
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

    public String getDonorLat() {
        return donorLat;
    }

    public void setDonorLat(String donorLat) {
        this.donorLat = donorLat;
    }

    public String getDonorLng() {
        return donorLng;
    }

    public void setDonorLng(String donorLng) {
        this.donorLng = donorLng;
    }
}
