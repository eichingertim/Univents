package com.androidproject.univents;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.androidproject.univents.customviews.EventItem;
import com.androidproject.univents.customviews.EventItemListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchQueryActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private Toolbar toolbar;

    private ListView listQuery;
    private EventItemListAdapter adapter;
    private List<EventItem> eventItems = new ArrayList<>();

    private Timestamp timestampFrom, timestampTo = null;
    private String category, city = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_query);

        initFirebase();
        initToolbar();
        getIntentExtras();
        initListViewAndAdapter();
        getData();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void initListViewAndAdapter() {
        listQuery = findViewById(R.id.list_search_query);
        adapter = new EventItemListAdapter(this, eventItems);
        listQuery.setAdapter(adapter);
        listQuery.setEmptyView(findViewById(R.id.tv_search_empty));
    }

    private void getIntentExtras() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        try {
            Date dateFrom = formatter.parse(getIntent().getStringExtra("searchDateFrom"));
            timestampFrom = new Timestamp(dateFrom);
            Date dateTo = formatter.parse(getIntent().getStringExtra("searchDateTo"));
            timestampTo = new Timestamp(dateTo);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!getIntent().getStringExtra("searchCategory").equals("Alle")) {
            category = getIntent().getStringExtra("searchCategory");
        }

        if (!getIntent().getStringExtra("searchCity").equals("")) {
            city = getIntent().getStringExtra("searchCity");
        }

    }

    private void getData() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            EventItem item = doc.toObject(EventItem.class);
                            eventItems.add(item);
                        }
                        checkTime();
                        checkCategory();
                        checkCity();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void checkCity() {
        if (city != null) {
            for (EventItem item : eventItems) {
                if (!item.getEventCity().equals(city)) {
                    eventItems.remove(item);
                }
            }
        }
    }

    private void checkCategory() {
        if (category != null) {
            for (EventItem item : eventItems) {
                if (!item.getEventCategory().equals(category)) {
                    eventItems.remove(item);
                }
            }
        }
    }

    private void checkTime() {
        if (timestampFrom != null) {
            for (EventItem item : eventItems) {
                if (item.getEventBegin().toDate().before(timestampFrom.toDate())) {
                    eventItems.remove(item);
                }
            }
        }
        if (timestampTo != null) {
            for (EventItem item : eventItems) {
                if (item.getEventEnd().toDate().after(timestampTo.toDate())) {
                    eventItems.remove(item);
                }
            }
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkTheme() {
        if (isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.DarkTheme);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.AppTheme);
        }
    }

    private boolean isDarkTheme() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences
                .getBoolean(getString(R.string.PREF_KEY_THEME), false);
    }
}
