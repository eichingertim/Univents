package com.androidproject.univents.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventSale;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the ExpandableListView which is displayed in the CreateEditSaleFragment
 */
public class EventSaleExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<EventSale> sales;

    public EventSaleExpandableListAdapter(Context context, List<EventSale> sales) {
        this.context = context;
        this.sales = sales;
    }

    @Override
    public int getGroupCount() {
        return sales.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return sales.get(groupPosition).getItems().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return sales.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<String> index = new ArrayList<>(sales.get(groupPosition).getItems().keySet());
        return index.get(childPosition) + ": "
                + sales.get(groupPosition).getItems().get(index.get(childPosition)) + " €";
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.layout_expand_list_view_category, null);
        }

        TextView tvCategory = view.findViewById(R.id.tv_expand_list_category);
        EventSale eventSale = (EventSale) getGroup(groupPosition);
        tvCategory.setText(eventSale.getCategory());

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.layout_expand_list_view_item,null);
        }

        TextView tvItem = view.findViewById(R.id.tv_expand_list_item);
        String item = (String) getChild(groupPosition, childPosition);
        tvItem.setText(item);

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
