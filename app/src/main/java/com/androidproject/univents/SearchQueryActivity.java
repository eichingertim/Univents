package com.androidproject.univents;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.androidproject.univents.customviews.EventItem;
import com.androidproject.univents.customviews.EventItemListAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
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
        initListViewAndAdapter();
        if (getIntent().getExtras().getBoolean("isSearchForTitle")) {
            getData();
        } else {
            getIntentExtras();
            getData();
        }
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
        listQuery.setDivider(null);
    }

    private void getIntentExtras() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        try {
            Date dateFrom = formatter.parse(getIntent().getStringExtra("searchDateFrom"));
            timestampFrom = new Timestamp(dateFrom);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
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
                        eventItems.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            EventItem item = doc.toObject(EventItem.class);
                            eventItems.add(item);
                        }
                        checkDateFrom();
                        checkDateTo();
                        checkCategory();
                        checkCity();
                        adapter.setList(eventItems);
                        try {
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void checkCity() {
        if (city != null) {
            ArrayList<EventItem> removeIds = new ArrayList<>();

            for (EventItem item : eventItems) {
                if (!item.getEventCity().equals(city)) {
                    removeIds.add(item);
                }
            }

            for (EventItem item : removeIds){
                eventItems.remove(item);
            }
        }
    }

    private void checkCategory() {
        if (category != null) {
            ArrayList<EventItem> removeIds = new ArrayList<>();

            for (EventItem item : eventItems) {
                if (!item.getEventCategory().equals(category)) {
                    removeIds.add(item);
                }
            }

            for (EventItem item : removeIds){
                eventItems.remove(item);
            }
        }
    }

    private void checkDateFrom() {
        if (timestampFrom != null) {
            ArrayList<EventItem> removeIds = new ArrayList<>();

            for (EventItem item : eventItems) {
                if (item.getEventBegin().toDate().before(timestampFrom.toDate())) {
                    removeIds.add(item);
                }
            }

            for (EventItem item : removeIds){
                eventItems.remove(item);
            }
        }
    }

    private void checkDateTo() {
        if (timestampTo != null) {
            ArrayList<EventItem> removeIds = new ArrayList<>();

            for (EventItem item : eventItems) {
                if (item.getEventEnd().toDate().after(timestampTo.toDate())) {
                    removeIds.add(item);
                }
            }

            for (EventItem item : removeIds){
                eventItems.remove(item);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_collapsable, menu);
        if (getIntent().getExtras().getBoolean("isSearchForTitle")) {
            menu.performIdentifierAction(R.id.action_menu_search, 0);
        }
        MenuItem menuItem = menu.findItem(R.id.action_menu_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
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
