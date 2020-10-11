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

/**
 * Adapter for the ListView which is displayed in the UpdatesFragment
 */
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

        fillViewsWithData(tvEventUpdateTitle, tvEventUpdateDescription
                , tvEventUpdateLikes, position);

        return view;
    }

    /**
     * Fill all given views with their belonging data
     * @param tvEventUpdateTitle textView for the update title
     * @param tvEventUpdateDescription textView for the update description
     * @param tvEventUpdateLikes textView for the update likes
     * @param position postion of the selected update-item
     */
    private void fillViewsWithData(TextView tvEventUpdateTitle, TextView tvEventUpdateDescription, TextView tvEventUpdateLikes, int position) {
        EventUpdate eventUpdate = updates.get(position);

        tvEventUpdateTitle.setText(eventUpdate.getEventUpdateTitle());
        tvEventUpdateDescription.setText(eventUpdate.getEventUpdateDescription());
        tvEventUpdateLikes.setText(String.valueOf(eventUpdate.getEventUpdateLikes()));
    }
}
