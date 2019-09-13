package com.androidproject.univents.controller;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Adapter for the ListView which is displayed in the SearchQueryActivity
 */
public class EventItemListAdapter extends BaseAdapter implements Filterable {

    Context context;

    //The list "itemsFull" contains all Events and the list "items" contains the queried
    //items
    List<EventItem> items;
    List<EventItem> itemsFull;

    public EventItemListAdapter(Context context, List<EventItem> items) {
        this.items = items;
        itemsFull = new ArrayList<>(items);
        this.context = context;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.layout_event_list_item, null);
        }

        final ImageView ivEventPicture = view.findViewById(R.id.img_list_item_picture);
        TextView tvEventTitle = view.findViewById(R.id.tv_list_item_title);
        TextView tvEventDate = view.findViewById(R.id.tv_list_item_date);
        TextView tvEventCity = view.findViewById(R.id.tv_list_item_city);

        fillViewsWithData(ivEventPicture, tvEventTitle, tvEventDate, tvEventCity, position);

        return view;
    }

    /**
     * Fills all given views with the data they belong to
     * @param ivEventPicture imageView for the event-title-picture
     * @param tvEventTitle textView for the event-title
     * @param tvEventDate textView for the event-date
     * @param tvEventCity textView for the event-city
     * @param position position of the current eventItem
     */
    private void fillViewsWithData(final ImageView ivEventPicture, TextView tvEventTitle, TextView tvEventDate, TextView tvEventCity, int position) {

        final EventItem item = items.get(position);

        //Calender which formats its date to the given pattern-string
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(item.getEventBegin().toDate());
        String date = DateFormat.format(context.getString(R.string.date_format_normal), calendar).toString() + " "
                + calendar.getTimeZone().getDisplayName(false, TimeZone.SHORT, Locale.getDefault());

        //With this ViewTreeObserver, we can check the height and width of the
        //given imageView and can set the picture to this size with PICASSO.
        ViewTreeObserver viewTreeObserver = ivEventPicture.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ivEventPicture.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalHeight = ivEventPicture.getMeasuredHeight();
                int finalWidth = ivEventPicture.getMeasuredWidth();
                Picasso.get().load(item.getEventPictureUrl())
                        .resize(finalWidth, finalHeight).centerCrop()
                        .into(ivEventPicture);
                return true;
            }
        });
        tvEventTitle.setText(item.getEventTitle());
        tvEventDate.setText(date);
        tvEventCity.setText(item.getEventCity());
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    /**
     * This filter filters the events on matching characters
     * and returns the resulted events
     */
    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<EventItem> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(itemsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (EventItem item : itemsFull) {
                    if (item.getEventTitle().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            items.clear();
            items.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public void setList(List<EventItem> itemsFull) {
        this.itemsFull = new ArrayList<>(itemsFull);
    }

}
