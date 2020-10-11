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

/**
 * Adapter for the ListView which is displayed in the QandAFragment
 */
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

        TextView tvQandAQuestion = view.findViewById(R.id.tv_event_qanda_question);
        TextView tvQandAAnswer = view.findViewById(R.id.tv_event_qanda_answer);
        TextView tvQandABoolAnswered = view.findViewById(R.id.tv_event_qanda_bool_answered);

        fillViewsWithData(tvQandAQuestion, tvQandAAnswer, tvQandABoolAnswered, position);

        return view;
    }

    /**
     * fills all given views with its their belonging data
     * @param tvQandAQuestion textView for the Question
     * @param tvQandAAnswer textView for the Answer
     * @param tvQandABoolAnswered textView for the text if the question is answered or not.
     * @param position position of the current qanda-item
     */
    private void fillViewsWithData(TextView tvQandAQuestion, TextView tvQandAAnswer
            , TextView tvQandABoolAnswered, int position) {
        EventQandA qandA = qandAs.get(position);

        tvQandAQuestion.setText(qandA.getQuestion());

        if (qandA.getAnswer() !=  null && !qandA.getAnswer().equals("")) {
            tvQandAAnswer.setText(qandA.getAnswer());
            tvQandAAnswer.setVisibility(View.VISIBLE);
            tvQandABoolAnswered.setText("Beantwortet");
            tvQandABoolAnswered.setTextColor(Color.parseColor("#4caf50"));
        } else {
            tvQandAAnswer.setVisibility(View.GONE);
            tvQandABoolAnswered.setText("Noch nicht beantwortet");
            tvQandABoolAnswered.setTextColor(Color.parseColor("#e64a19"));
        }
    }
}
