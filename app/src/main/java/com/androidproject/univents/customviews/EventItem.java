package com.androidproject.univents.customviews;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class EventItem {

    private String title;
    private String description;
    private String organizer;
    private String eventID;
    private Timestamp begin;
    private Timestamp end;
    private String picture_url;
    private GeoPoint exact_location;
    private String descr_location;

    public EventItem() {}

    public EventItem(String title, String description, String organizer, String eventID
            , Timestamp begin, Timestamp end, String picture_url, GeoPoint exact_location,
                     String descr_location) {
        this.title = title;
        this.description = description;
        this.organizer = organizer;
        this.eventID = eventID;
        this.begin = begin;
        this.end = end;
        this.picture_url = picture_url;
        this.exact_location = exact_location;
        this.descr_location = descr_location;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getOrganizer() {
        return organizer;
    }

    public String getEventID() {
        return eventID;
    }

    public Timestamp getBegin() {
        return begin;
    }

    public Timestamp getEnd() {
        return end;
    }

    public String getPicture_url() {
        return picture_url;
    }

    public GeoPoint getExact_location() {
        return exact_location;
    }

    public String getDescr_location() {
        return descr_location;
    }
}
