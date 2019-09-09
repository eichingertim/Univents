package com.androidproject.univents.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventUpdate;

import java.util.List;

public class EventUpdateListAdapter extends BaseAdapter {

    List<EventUpdate> updates;
    Context context;

    public EventUpdateListAdapter(Context context, List<EventUpdate> updates) {
        this.updates = updates;
        this.context = context;
    }

    @Override
    public int getCount() {
        return updates.size();
    }

    @Override
    public Object getItem(int position) {
        return updates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.layout_event_update_item, null);
        }

        TextView tvEventUpdateTitle = view.findViewById(R.id.tv_event_update_title);
        TextView tvEventUpdateDescription = view.findViewById(R.id.tv_event_update_description);
        TextView tvEventUpdateLikes = view.findViewById(R.id.tv_event_update_likes);

        EventUpdate eventUpdate = updates.get(position);

        tvEventUpdateTitle.setText(eventUpdate.getEventUpdateTitle());
        tvEventUpdateDescription.setText(eventUpdate.getEventUpdateDescription());
        tvEventUpdateLikes.setText(String.valueOf(eventUpdate.getEventUpdateLikes()));

        return view;
    }
}
