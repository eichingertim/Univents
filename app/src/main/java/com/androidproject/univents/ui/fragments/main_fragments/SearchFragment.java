package com.androidproject.univents.ui.fragments.main_fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.ui.CreateEditEventActivity;
import com.androidproject.univents.ui.SearchQueryActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;

public class SearchFragment extends Fragment {

    private TextView tvDateFrom;
    private TextView tvDateTo;

    private Spinner spCategory;
    private ArrayAdapter<CharSequence> adapter;

    private EditText txtCity;

    private Button btnSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    /**
     * initializes the views from the UI and sets the listeners
     * @param view current fragment-layout
     */
    private void initViews(View view) {
        initDateFields(view);
        initCategorySpinner(view);
        initCityEditText(view);
        initFloatingSearchButton(view);
    }

    /**
     * initializes the action button where the filtered search is executed and
     * sets an onClickListener
     * @param view current fragment-layout
     */
    private void initFloatingSearchButton(View view) {
        btnSearch = view.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
    }

    /**
     * initializes the editText where the user can enter a city to
     * filter the search.
     * @param view current fragment-layout
     */
    private void initCityEditText(View view) {
        txtCity = view.findViewById(R.id.txt_city);
    }

    /**
     * initializes the Spinner and its adapter where the user can select a category
     * for the search.
     * @param view current fragment-layout
     */
    private void initCategorySpinner(View view) {
        spCategory = view.findViewById(R.id.sp_category);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.eventCategorys, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    /**
     * initializes the two date fields where the user can select specific dates and
     * sets onClickListeners.
     * @param view current fragment-layout
     */
    private void initDateFields(View view) {
        tvDateFrom = view.findViewById(R.id.tv_date_from);
        tvDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAndSetDate(tvDateFrom);
            }
        });
        tvDateTo = view.findViewById(R.id.tv_date_to);
        tvDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAndSetDate(tvDateTo);
            }
        });
    }

    /**
     * gets the data from the filter items (date, category, city) and puts it
     * into an intent that is send to the QueryActivity.
     */
    private void search() {
        String dateFrom = tvDateFrom.getText().toString();
        String dateTo = tvDateTo.getText().toString();
        String categorySelected = adapter.getItem(spCategory.getSelectedItemPosition()).toString();
        String city = "";
        if (!txtCity.getText().toString().isEmpty()) {
            city = txtCity.getText().toString();
        }
        Intent searchIntent = new Intent(getActivity(), SearchQueryActivity.class);
        searchIntent.putExtra("searchDateFrom", dateFrom);
        searchIntent.putExtra("searchDateTo", dateTo);
        searchIntent.putExtra("searchCategory", categorySelected);
        searchIntent.putExtra("searchCity", city);
        searchIntent.putExtra("isSearchForTitle", false);
        startActivity(searchIntent);
    }

    /**
     * Opens a dialog to set the date.
     * @param tvDate textView where the date should be written to.
     */
    private void getAndSetDate(final TextView tvDate) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        getActivity().setTheme(R.style.AppTheme);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        ((CreateEditEventActivity)getActivity()).checkTheme();
                        writeDateToTextView(year, monthOfYear, dayOfMonth, tvDate);

                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * writes the selected date to the textView tvDate
     * @param year selected year
     * @param monthOfYear selected month
     * @param dayOfMonth selected day
     * @param tvDate textView where the date should be written to
     */
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_menu_search:
                Intent intent = new Intent(getActivity(), SearchQueryActivity.class);
                intent.putExtra("isSearchForTitle", true);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


}
