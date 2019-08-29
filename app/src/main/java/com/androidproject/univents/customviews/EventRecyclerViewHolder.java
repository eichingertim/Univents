package com.androidproject.univents.customviews;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class EventRecyclerViewHolder extends RecyclerView.ViewHolder {

    public ImageView imgEventPicture;
    public TextView tvEventTitle;
    public TextView tvEventDate;

    public EventRecyclerViewHolder(View itemView) {
        super(itemView);
        imgEventPicture = itemView.findViewById(R.id.img_card_event_picture);
        tvEventTitle = itemView.findViewById(R.id.tv_card_event_title);
        tvEventDate = itemView.findViewById(R.id.tv_card_event_date_time);

    }

    public void bind(final EventItem item, final EventItemRecyclerAdapter.OnItemClickListener listener) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(item.getEventBegin().toDate());
        String date = DateFormat.format("dd.MM.yyyy - HH.mm", calendar).toString() + " "
                + calendar.getTimeZone().getDisplayName(false, TimeZone.SHORT, Locale.getDefault());

        Picasso.get().load(item.getEventPictureUrl())
                .resize(400,200).centerCrop()
                .into(imgEventPicture);
        tvEventTitle.setText(item.getEventTitle());
        tvEventDate.setText(date);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item);
            }
        });
    }

}