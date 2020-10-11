package com.androidproject.univents.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventItem;
import com.androidproject.univents.controller.EventItemListAdapter;
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

/**
 * In this Activity the results of a search query or a filtered search are displayed
 */
public class SearchQueryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private FirebaseFirestore db;

    private Toolbar toolbar;

    private ListView listQuery;
    private EventItemListAdapter adapter;
    private List<EventItem> eventItems = new ArrayList<>();

    private Timestamp timestampFrom, timestampTo = null;
    private String city = null;
    private String category = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_query);

        initFirebase();
        initToolbar();
        initListViewAndAdapter();
        if (getIntent().getExtras().getBoolean(getString(R.string.KEY_INTENT_SEARCH_FOR_TITLE))) {
            getData();
        } else {
            getIntentExtras();
            getData();
        }
    }

    /**
     * initializes necessary firebase-tools
     */
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * initializes the toolbar as an actionbar and sets backIcon available
     */
    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * initializes the listView for the query and its adapter
     */
    private void initListViewAndAdapter() {
        listQuery = findViewById(R.id.list_search_query);
        adapter = new EventItemListAdapter(this, eventItems);
        listQuery.setAdapter(adapter);
        listQuery.setEmptyView(findViewById(R.id.tv_search_empty));
        listQuery.setDivider(null);
        listQuery.setOnItemClickListener(this);
    }

    /**
     * checks of the intent data are valid for the needed keys, if yes
     * the corresponding objects are initialized with the data
     */
    private void getIntentExtras() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        try {
            Date dateFrom = formatter.parse(getIntent().getStringExtra(getString(R.string.KEY_INTENT_SEARCH_DATE_FROM)));
            timestampFrom = new Timestamp(dateFrom);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            Date dateTo = formatter.parse(getIntent().getStringExtra(getString(R.string.KEY_INTENT_SEARCH_DATE_TO)));
            timestampTo = new Timestamp(dateTo);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!getIntent().getStringExtra(getString(R.string.KEY_INTENT_SEARCH_CATEGORY)).equals("Alle")) {
            category = getIntent().getStringExtra(getString(R.string.KEY_INTENT_SEARCH_CATEGORY));
        }

        if (!getIntent().getStringExtra(getString(R.string.KEY_INTENT_SEARCH_CITY)).equals("")) {
            city = getIntent().getStringExtra(getString(R.string.KEY_INTENT_SEARCH_CITY));
        }

    }

    /**
     * gets all events from the firebase-cloud and saves it in a list of eventItems.
     * Then it checks for every search-criteria if the eventItem is valid or not and
     * updates the list.
     */
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

    /**
     * removes all items that don't match the city-filter
     */
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

    /**
     * removes all items that don't match the category-filter
     */
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

    /**
     * removes all items that don't match the date-from-filter
     */
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

    /**
     * removes all items that don't match the date-to-filter
     */
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
        if (getIntent().getExtras().getBoolean(getString(R.string.KEY_INTENT_IS_SEARCH_TITLE))) {
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

    /**
     * checks whether dark or light theme is enabled
     */
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String eventId = ((EventItem) parent.getItemAtPosition(position)).getEventId();

        Intent showEventIntent = new Intent(SearchQueryActivity.this, ShowEventActivity.class);
        showEventIntent.putExtra(getString(R.string.KEY_FIREBASE_EVENT_ID), eventId);
        startActivity(showEventIntent);
    }
}
