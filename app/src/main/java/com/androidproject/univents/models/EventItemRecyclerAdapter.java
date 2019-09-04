package com.androidproject.univents.models;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidproject.univents.R;

import java.util.List;

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
