package com.androidproject.univents.models;

public class EventUpdate {

    private String eventTitle;
    private String eventUpdateTitle;
    private String eventUpdateDescription;
    private int eventUpdateLikes;
    private String eventUpdateId;

    private EventUpdate() {

    }

    public EventUpdate(String eventTitle, String eventUpdateTitle, String eventUpdateDescription
            , int eventUpdateLikes, String eventUpdateId) {
        this.eventTitle = eventTitle;
        this.eventUpdateTitle = eventUpdateTitle;
        this.eventUpdateDescription = eventUpdateDescription;
        this.eventUpdateLikes = eventUpdateLikes;
        this.eventUpdateId = eventUpdateId;
    }

    public String getEventUpdateId() {
        return eventUpdateId;
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
