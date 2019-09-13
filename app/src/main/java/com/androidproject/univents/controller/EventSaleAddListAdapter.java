package com.androidproject.univents.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.androidproject.univents.R;

import java.util.ArrayList;

/**
 * Adapter for the listView which is displayd in the CreateEditSaleListsActivity
 */
public class EventSaleAddListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> identifier;
    private ArrayList<Double> price;

    public EventSaleAddListAdapter(Context context, ArrayList<String> identifier
            , ArrayList<Double> price) {
        this.context = context;
        this.identifier = identifier;
        this.price = price;
    }

    @Override
    public int getCount() {
        return identifier.size();
    }

    @Override
    public Object getItem(int position) {
        return identifier.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.layout_event_sale_add_list_item
                    ,null);

        }

        ImageButton btnRemoveItem = view.findViewById(R.id.btn_remove_sale_list_item);
        TextView tvSaleListItem = view.findViewById(R.id.tv_sale_list_item);

        fillViewsWithDataAndSetClickListener(btnRemoveItem, tvSaleListItem, position);
        return view;
    }

    /**
     * fills the given views with their belonging data and sets an onClickListener
     * to the "removeButton"
     * @param btnRemoveItem button where the user can remove a sale-list-item
     * @param tvSaleListItem textView for the sale-list-item, where the identifier and
     *                       price is displayed.
     * @param position position of the sale-list-item
     */
    private void fillViewsWithDataAndSetClickListener(ImageButton btnRemoveItem
            , TextView tvSaleListItem, final int position) {

        String item  = identifier.get(position) + ": " + price.get(position) + " €";
        tvSaleListItem.setText(item);

        btnRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(position);
            }
        });
    }

    private void removeItem(int position) {
        identifier.remove(position);
        price.remove(position);
        notifyDataSetChanged();
    }
}
