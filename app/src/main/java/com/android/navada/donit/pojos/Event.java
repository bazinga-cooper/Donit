package com.android.navada.donit.pojos;

public class Event {

    private String name;
    private String description;
    private String imageURL;
    private Long timeStamp;
    private String organization;

    public Event() {}

    public Event(String name, String description, String imageURL, Long timeStamp, String organization) {
        this.name = name;
        this.description = description;
        this.imageURL = imageURL;
        this.timeStamp = timeStamp;
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public String getOrganization() {
        return organization;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

}
