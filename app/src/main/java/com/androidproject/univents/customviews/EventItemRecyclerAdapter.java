package com.androidproject.univents.customviews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventItemRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(EventItem item);
    }

    List<EventItem> items;
    Context context;
    private final OnItemClickListener listener;



    public EventItemRecyclerAdapter(Context context, List<EventItem> items
            , OnItemClickListener listener) {
        this.items = items;
        this.context = context;
        this.listener = listener;
    }


    @NonNull
    @Override
    public EventRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_event_recycler_card, viewGroup, false);
        EventRecyclerViewHolder holder = new EventRecyclerViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventRecyclerViewHolder eventRecyclerViewHolder, int position) {
        EventItem item = items.get(position);
        eventRecyclerViewHolder.bind(item, listener);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
