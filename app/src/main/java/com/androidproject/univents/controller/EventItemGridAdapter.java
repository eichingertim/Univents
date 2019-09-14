package com.androidproject.univents.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.androidproject.univents.models.EventItem;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Adapter for the GridView which is displayed in the HomeFragment
 */
public class EventItemGridAdapter extends BaseAdapter {

    private Context context;
    private List<EventItem> items;

    /**
     * Small floating ActionButton, where the user can share an event, when he clicks on it.
     */
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

        ImageView ivEventPicture = view.findViewById(R.id.img_card_event_picture);
        TextView tvEventTitle = view.findViewById(R.id.tv_card_event_title);
        TextView tvEventDateTime = view.findViewById(R.id.tv_card_event_date_time);
        TextView tvEventLocationDetail = view.findViewById(R.id.tv_card_event_descr_location);
        btnShareEvent = view.findViewById(R.id.card_share);

        fillViewsWithData(ivEventPicture, tvEventTitle, tvEventDateTime, tvEventLocationDetail, position);

        return view;

    }

    /**
     * Fills all given views with the data they belong to.
     * @param ivEventPicture imageView for the event-title-picture
     * @param tvEventTitle textView for the event-title
     * @param tvEventDateTime textView for the event's date and time
     * @param tvEventLocationDetail textView for the event-description
     * @param position position of the current/selected event
     */
    private void fillViewsWithData(final ImageView ivEventPicture, TextView tvEventTitle
            , TextView tvEventDateTime, TextView tvEventLocationDetail, int position) {
        final EventItem event = items.get(position);

        //Calender which formats its date to the given pattern-string
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(event.getEventBegin().toDate());
        String date = DateFormat.format(context.getString(R.string.date_format_normal), calendar).toString() + " "
                + calendar.getTimeZone().getDisplayName(false, TimeZone.SHORT, Locale.getDefault());
        handleOnClickShare(event);

        //With this ViewTreeObserver, we can check the height and width of the
        //given imageView and can set the picture to this size with PICASSO.
        ViewTreeObserver viewTreeObserver = ivEventPicture.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ivEventPicture.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalHeight = ivEventPicture.getMeasuredHeight();
                int finalWidth = ivEventPicture.getMeasuredWidth();
                Picasso.get().load(event.getEventPictureUrl())
                        .resize(finalWidth, finalHeight).centerCrop()
                        .into(ivEventPicture);
                return true;
            }
        });

        tvEventTitle.setText(event.getEventTitle());
        tvEventDateTime.setText(date);
        tvEventLocationDetail.setText(event.getEventDetailLocation());
    }

    /**
     * initializes strings with their belonging data
     * and creates a message for the dynamic link to send
     * @param eventItem pressed item
     */
    private void handleOnClickShare(final EventItem eventItem) {
        btnShareEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventName = eventItem.getEventTitle();
                String eventID = eventItem.getEventId();
                String eventDescr = eventItem.getEventDescription();

                String message = String.format(context.getString(R.string.dynamic_link_text), eventName)
                        + buildDynamicLink(eventID, eventName, eventDescr);
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
                .setLink(Uri.parse("https://ttjappproject.wixsite.com/univents"+eventID))
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
