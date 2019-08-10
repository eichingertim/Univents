package com.androidproject.univents.user;

public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String userId;
    private boolean isOrga;
    private String orgaName = "";

    public User() {

    }

    public User(String foreName, String lastName, String email, String userId, boolean isOrga, String orgaName) {
        this.firstName = foreName;
        this.lastName = lastName;
        this.email = email;
        this.userId = userId;
        this.isOrga = isOrga;
        this.orgaName = orgaName;
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

    public boolean isOrga() {
        return isOrga;
    }

    public void setOrga(boolean orga) {
        isOrga = orga;
    }

    public String getOrgaName() {
        return orgaName;
    }

    public void setOrgaName(String orgaName) {
        this.orgaName = orgaName;
    }
}