package com.androidproject.univents.models;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventItemListAdapter extends BaseAdapter implements Filterable {

    List<EventItem> items;
    List<EventItem> itemsFull;
    Context context;

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

        final ImageView imgEventPicture = view.findViewById(R.id.img_list_item_picture);
        TextView tvEventTitle = view.findViewById(R.id.tv_list_item_title);
        TextView tvEventDate = view.findViewById(R.id.tv_list_item_date);
        TextView tvEventCity = view.findViewById(R.id.tv_list_item_city);

        final EventItem item = items.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(item.getEventBegin().toDate());
        String date = DateFormat.format("dd.MM.yyyy - HH.mm", calendar).toString() + " "
                + calendar.getTimeZone().getDisplayName(false, TimeZone.SHORT, Locale.getDefault());

        ViewTreeObserver viewTreeObserver = imgEventPicture.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imgEventPicture.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalHeight = imgEventPicture.getMeasuredHeight();
                int finalWidth = imgEventPicture.getMeasuredWidth();
                Picasso.get().load(item.getEventPictureUrl())
                        .resize(finalWidth, finalHeight).centerCrop()
                        .into(imgEventPicture);
                return true;
            }
        });
        tvEventTitle.setText(item.getEventTitle());
        tvEventDate.setText(date);
        tvEventCity.setText(item.getEventCity());

        return view;
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

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
