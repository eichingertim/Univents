package com.androidproject.univents.models;

public class EventUpdate {

    private String eventTitle;
    private String eventUpdateTitle;
    private String eventUpdateDescription;
    private int eventUpdateLikes;

    private EventUpdate() {

    }

    public EventUpdate(String eventTitle, String eventUpdateTitle, String eventUpdateDescription
            , int eventUpdateLikes) {
        this.eventTitle = eventTitle;
        this.eventUpdateTitle = eventUpdateTitle;
        this.eventUpdateDescription = eventUpdateDescription;
        this.eventUpdateLikes = eventUpdateLikes;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getEventUpdateTitle() {
        return eventUpdateTitle;
    }

    public String getEventUpdateDescription() {
        return eventUpdateDescription;
    }

    public int getEventUpdateLikes() {
        return eventUpdateLikes;
    }
}
