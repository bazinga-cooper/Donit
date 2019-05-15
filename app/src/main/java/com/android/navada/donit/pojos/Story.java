package com.android.navada.donit.pojos;

public class Story {

    private String addedBy;
    private String mainContent;
    private String imageURL;
    private String timeStamp;

    public Story(String addedBy, String mainContent, String imageURL, String timeStamp) {
        this.addedBy = addedBy;
        this.mainContent = mainContent;
        this.imageURL = imageURL;
        this.timeStamp = timeStamp;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public String getMainContent() {
        return mainContent;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public void setMainContent(String mainContent) {
        this.mainContent = mainContent;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
