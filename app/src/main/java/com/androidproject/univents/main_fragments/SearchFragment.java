package com.androidproject.univents.main_fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.androidproject.univents.SearchQueryActivity;

import java.util.Calendar;

public class SearchFragment extends Fragment {

    private TextView tvDateFrom;
    private TextView tvDateTo;

    private Spinner spCategory;
    private ArrayAdapter<CharSequence> spAdapter;

    private EditText txtCity;

    private FloatingActionButton fabSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setOnClickListeners();
    }

    private void initViews(View view) {
        tvDateFrom = view.findViewById(R.id.tv_date_from);
        tvDateTo = view.findViewById(R.id.tv_date_to);

        spCategory = view.findViewById(R.id.sp_category);
        spAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.eventCategorys
                , android.R.layout.simple_spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(spAdapter);

        txtCity = view.findViewById(R.id.txt_city);

        fabSearch = view.findViewById(R.id.fab_search);
    }

    private void setOnClickListeners() {
        tvDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAndSetDate(tvDateFrom);
            }
        });
        tvDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAndSetDate(tvDateTo);
            }
        });
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
    }

    private void search() {
        String dateFrom = tvDateFrom.getText().toString();
        String dateTo = tvDateTo.getText().toString();
        String category = spAdapter.getItem(spCategory.getSelectedItemPosition()).toString();
        String city = "";
        if (!txtCity.getText().toString().isEmpty()) {
            city = txtCity.getText().toString();
        }
        Intent searchIntent = new Intent(getActivity(), SearchQueryActivity.class);
        searchIntent.putExtra("searchDateFrom", dateFrom);
        searchIntent.putExtra("searchDateTo", dateTo);
        searchIntent.putExtra("searchCategory", category);
        searchIntent.putExtra("searchCity", city);
        startActivity(searchIntent);
    }

    private void getAndSetDate(final TextView tvDate) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        writeDateToTextView(year, monthOfYear, dayOfMonth, tvDate);

                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void writeDateToTextView(int year, int monthOfYear, int dayOfMonth, TextView tvDate) {
        int realMonthOfYear = monthOfYear + 1;

        String day = String.valueOf(dayOfMonth);
        String month = String.valueOf(realMonthOfYear);
        if (monthOfYear < 10) {
            month = "0"+realMonthOfYear;
        }
        if (dayOfMonth < 10) {
            day = "0"+dayOfMonth;
        }

        String date = day + "." + month + "." + year;
        tvDate.setText(date);
    }

}
