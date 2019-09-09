package com.androidproject.univents.controller;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventQandA;

import java.util.ArrayList;

public class EventQandAListAdapter extends BaseAdapter {

    private ArrayList<EventQandA> qandAs;
    private Context context;

    public EventQandAListAdapter(Context context, ArrayList<EventQandA> qandAs) {
        this.qandAs = qandAs;
        this.context = context;
    }

    @Override
    public int getCount() {
        return qandAs.size();
    }

    @Override
    public Object getItem(int position) {
        return qandAs.get(position);
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
            view = layoutInflater.inflate(R.layout.layout_event_qanda_item, null);
        }

        TextView tvQuestion = view.findViewById(R.id.tv_event_qanda_question);
        TextView tvAnswer = view.findViewById(R.id.tv_event_qanda_answer);
        TextView tvBoolAnswered = view.findViewById(R.id.tv_event_qanda_bool_answered);

        EventQandA qandA = qandAs.get(position);

        tvQuestion.setText(qandA.getQuestion());

        if (qandA.getAnswer() !=  null && !qandA.getAnswer().equals("")) {
            tvAnswer.setText(qandA.getAnswer());
            tvAnswer.setVisibility(View.VISIBLE);
            tvBoolAnswered.setText("Beantwortet");
            tvBoolAnswered.setTextColor(Color.parseColor("#4caf50"));
        } else {
            tvAnswer.setVisibility(View.GONE);
            tvBoolAnswered.setText("Noch nicht beantwortet");
            tvBoolAnswered.setTextColor(Color.parseColor("#e64a19"));
        }

        return view;
    }
}
