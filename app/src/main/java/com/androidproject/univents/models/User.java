package com.androidproject.univents.models;

public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String userId;
    private boolean isOrga;
    private String orgaName = "";
    private String description = "Keine Beschreibung vorhanden";
    private String phoneNumber = "";
    private String pictureURL = "";

    public User() {

    }

    public User(String firstName, String lastName, String email, String userId
            , boolean isOrga, String orgaName, String description, String phoneNumber, String pictureURL) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userId = userId;
        this.isOrga = isOrga;
        this.orgaName = orgaName;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.pictureURL = pictureURL;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean getOrga() {
        return isOrga;
    }

    public void setOrga(boolean orga) {
        this.isOrga = orga;
    }

    public String getOrgaName() {
        return orgaName;
    }

    public void setOrgaName(String orgaName) {
        this.orgaName = orgaName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }
}