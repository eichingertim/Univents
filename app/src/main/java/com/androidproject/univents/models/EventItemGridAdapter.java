package com.androidproject.univents.models;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventItemGridAdapter extends BaseAdapter {

    private Context context;
    private List<EventItem> items;

    private ImageButton btnShareEvent;

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

        final ImageView imgPicture = view.findViewById(R.id.img_card_event_picture);
        TextView tvTitle = view.findViewById(R.id.tv_card_event_title);
        TextView tvDateTime = view.findViewById(R.id.tv_card_event_date_time);
        TextView tvDescrLocation = view.findViewById(R.id.tv_card_event_descr_location);
        btnShareEvent = view.findViewById(R.id.card_share);

        final EventItem event = items.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(event.getEventBegin().toDate());
        String date = DateFormat.format("dd.MM.yyyy - HH.mm", calendar).toString() + " "
                + calendar.getTimeZone().getDisplayName(false, TimeZone.SHORT, Locale.getDefault());
        handleOnClickShare(event);

        ViewTreeObserver viewTreeObserver = imgPicture.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imgPicture.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalHeight = imgPicture.getMeasuredHeight();
                int finalWidth = imgPicture.getMeasuredWidth();
                Picasso.get().load(event.getEventPictureUrl())
                        .resize(finalWidth, finalHeight).centerCrop()
                        .into(imgPicture);
                return true;
            }
        });

        tvTitle.setText(event.getEventTitle());
        tvDateTime.setText(date);
        tvDescrLocation.setText(event.getEventDetailLocation());

        return view;

    }

    /**
     * handles when the user presses the share button of an item
     * @param eventItem pressed item
     */
    private void handleOnClickShare(final EventItem eventItem) {
        btnShareEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventName = eventItem.getEventTitle();
                String eventID = eventItem.getEventId();
                String eventDescr = eventItem.getEventDescription();

                String message = "Hi, schau dir das Event " + eventName
                        + " an. \n" + buildDynamicLink(eventID, eventName, eventDescr);
                shareDynamicLink(message);
            }
        });
    }

    /**
     * builds the dynamic link
     * @param eventID id of the event
     * @param eventTitle title of the event
     * @param eventDescr description of the event
     * @return returns a URI with the generated dynamic link
     */
    private Uri buildDynamicLink(String eventID, String eventTitle, String eventDescr) {
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.univents.com/"+eventID))
                .setDomainUriPrefix("https://univents.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .setIosParameters(new DynamicLink.IosParameters.Builder("com.androidproject.univents").build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(eventTitle)
                                .setDescription(eventDescr)
                                .build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();
        return dynamicLinkUri;
    }

    /**
     * shares the link and message with intent, that opens an app
     * who can handle the intent.
     * @param message includes the message with dynamic link
     */
    private void shareDynamicLink(String message) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setType("text/plain");
        context.startActivity(intent);

    }
}