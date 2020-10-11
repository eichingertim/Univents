package com.androidproject.univents.controller;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventItem;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * ViewHolder for the EventItemRecyclerAdapter
 */
public class EventRecyclerViewHolder extends RecyclerView.ViewHolder {

    private Context context;

    public ImageView imgEventPicture;
    public TextView tvEventTitle;
    public TextView tvEventDate;

    public EventRecyclerViewHolder(Context context, View itemView) {
        super(itemView);
        this.context = context;
        imgEventPicture = itemView.findViewById(R.id.img_card_event_picture);
        tvEventTitle = itemView.findViewById(R.id.tv_card_event_title);
        tvEventDate = itemView.findViewById(R.id.tv_card_event_date_time);

    }

    public void bind(EventItem eventItem, EventItemRecyclerAdapter.OnItemClickListener listener) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(eventItem.getEventBegin().toDate());
        String date = DateFormat.format(context.getString(R.string.date_format_normal), calendar).toString() + " "
                + calendar.getTimeZone().getDisplayName(false, TimeZone.SHORT, Locale.getDefault());


        fillViewsWithData(eventItem, date, listener);
    }

    /**
     * fills the given views with their belonging data
     * @param eventItem current selected eventItem
     * @param date configured date of the event
     * @param listener clickListener for the recyclerView
     */
    private void fillViewsWithData(final EventItem eventItem, String date
            , final EventItemRecyclerAdapter.OnItemClickListener listener) {

        //With this ViewTreeObserver, we can check the height and width of the
        //given imageView and can set the picture to this size with PICASSO.
        ViewTreeObserver viewTreeObserver = imgEventPicture.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imgEventPicture.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalHeight = imgEventPicture.getMeasuredHeight();
                int finalWidth = imgEventPicture.getMeasuredWidth();
                Picasso.get().load(eventItem.getEventPictureUrl())
                        .resize(finalWidth, finalHeight).centerCrop()
                        .into(imgEventPicture);
                return true;
            }
        });

        tvEventTitle.setText(eventItem.getEventTitle());
        tvEventDate.setText(date);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(eventItem);
            }
        });
    }

}
