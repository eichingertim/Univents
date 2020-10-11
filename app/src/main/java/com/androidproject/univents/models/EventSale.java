package com.androidproject.univents.models;

import java.util.Map;

public class EventSale {

    private String category;
    private Map<String, Object> items;


    public EventSale(String category, Map<String, Object> items) {
        this.category = category;
        this.items = items;
    }

    public String getCategory() {
        return category;
    }

    public Map<String, Object> getItems() {
        return items;
    }
}
