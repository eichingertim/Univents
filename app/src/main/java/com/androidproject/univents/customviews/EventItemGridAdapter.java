package com.androidproject.univents.customviews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidproject.univents.R;

import java.util.Calendar;
import java.util.List;

public class EventItemGridAdapter extends BaseAdapter {

    private Context context;
    private List<EventItem> items;

    public EventItemGridAdapter(@NonNull Context context, List<EventItem> items) {

        this.context = context;
        this.items = items;

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.layout_event_card, null);
        }

        ImageView imgPicture = view.findViewById(R.id.img_card_event_picture);
        TextView tvTitle = view.findViewById(R.id.tv_card_event_title);
        TextView tvDateTime = view.findViewById(R.id.tv_card_event_date_time);
        TextView tvDescrLocation = view.findViewById(R.id.tv_card_event_descr_location);

        EventItem event = items.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(event.getBegin().toDate());
        String date = DateFormat.format("dd.MM.yyyy - hh.mm", calendar).toString() + " MESZ";

        //Picasso.get().load(event.getPicture_url()).into(imgPicture);
        tvTitle.setText(event.getTitle());
        tvDateTime.setText(date);
        tvDescrLocation.setText(event.getDescr_location());

        return view;

    }
}
