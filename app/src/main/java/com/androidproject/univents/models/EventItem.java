package com.androidproject.univents.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class EventItem {

    private String eventTitle;
    private String eventDescription;
    private String eventOrganizer;
    private String eventId;
    private Timestamp eventBegin;
    private Timestamp eventEnd;
    private String eventPictureUrl;
    private GeoPoint eventExactLocation;
    private String eventDetailLocation;
    private String eventCity;
    private String eventCategory;
    private List<String> eventParticipants;

    public EventItem() {}

    public EventItem(String eventTitle, String eventDescription, String eventOrganizer, String eventId
            , Timestamp eventBegin, Timestamp eventEnd, String eventPictureUrl, GeoPoint eventExactLocation,
                     String eventDetailLocation, String eventCity, String eventCategory
            ,List<String> eventParticipants) {
        this.eventTitle = eventTitle;
        this.eventDescription = eventDescription;
        this.eventOrganizer = eventOrganizer;
        this.eventId = eventId;
        this.eventBegin = eventBegin;
        this.eventEnd = eventEnd;
        this.eventPictureUrl = eventPictureUrl;
        this.eventExactLocation = eventExactLocation;
        this.eventDetailLocation = eventDetailLocation;
        this.eventCity = eventCity;
        this.eventCategory = eventCategory;
        this.eventParticipants = eventParticipants;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getEventOrganizer() {
        return eventOrganizer;
    }

    public String getEventId() {
        return eventId;
    }

    public Timestamp getEventBegin() {
        return eventBegin;
    }

    public Timestamp getEventEnd() {
        return eventEnd;
    }

    public String getEventPictureUrl() {
        return eventPictureUrl;
    }

    public GeoPoint getEventExactLocation() {
        return eventExactLocation;
    }

    public String getEventDetailLocation() {
        return eventDetailLocation;
    }

    public String getEventCity() {
        return eventCity;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public List<String> getEventParticipants() {
        return eventParticipants;
    }
}
