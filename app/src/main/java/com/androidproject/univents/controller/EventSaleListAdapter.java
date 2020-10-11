package com.androidproject.univents.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventSale;
import com.androidproject.univents.models.ExpandedGridView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Adapter for the ListView which is displayed in the SaleFragment
 */
public class EventSaleListAdapter extends BaseAdapter {

    private ArrayList<EventSale> sales;
    private Context context;

    public EventSaleListAdapter(Context context, ArrayList<EventSale> sales) {
        this.sales = sales;
        this.context = context;
    }

    @Override
    public int getCount() {
        return sales.size();
    }

    @Override
    public Object getItem(int position) {
        return sales.get(position);
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
            view = layoutInflater.inflate(R.layout.layout_event_sale_item, null);
        }

        TextView tvEventSaleTitle = view.findViewById(R.id.tv_event_sale_title);
        ExpandedGridView lvEventSaleItems = view.findViewById(R.id.lv_event_sale_items);

        fillViewsWithData(tvEventSaleTitle, lvEventSaleItems, position);

        return view;
    }

    /**
     * fills all given views with their belonging data
     * @param tvEventSaleTitle textView for the category/title of one sale-list
     * @param lvEventSaleItems listView for all the items of one sale-list
     * @param position position of the sale-list
     */
    private void fillViewsWithData(TextView tvEventSaleTitle
            , ExpandedGridView lvEventSaleItems, int position) {
        EventSale sale = sales.get(position);
        ArrayList<String> itemWithPrice = new ArrayList<>();

        for (Map.Entry<String,Object> entry : sale.getItems().entrySet()) {
            itemWithPrice.add(entry.getKey() + ": " + entry.getValue().toString() + "€");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, itemWithPrice);
        lvEventSaleItems.setAdapter(adapter);

        tvEventSaleTitle.setText(sale.getCategory());
    }
}
